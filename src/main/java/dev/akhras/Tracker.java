package dev.akhras;
import java.nio.file.*;import java.security.*;import java.sql.*;import java.io.*;import java.util.*;import java.time.*;
public class Tracker implements AutoCloseable{
 final Connection db;
 public Tracker(String file)throws SQLException{db=DriverManager.getConnection("jdbc:sqlite:"+file);try(var s=db.createStatement()){s.execute("CREATE TABLE IF NOT EXISTS evidence(id INTEGER PRIMARY KEY,path TEXT,hash TEXT)");s.execute("CREATE TABLE IF NOT EXISTS events(id INTEGER PRIMARY KEY,evidence_id INTEGER,payload TEXT,previous_hash TEXT,hash TEXT)");}}
 public static String hash(byte[] bytes){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 public static String fingerprint(Path p)throws IOException{try(var in=Files.newInputStream(p)){try{var digest=MessageDigest.getInstance("SHA-256");byte[]buffer=new byte[65536];for(int n;(n=in.read(buffer))!=-1;)digest.update(buffer,0,n);return HexFormat.of().formatHex(digest.digest());}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}}
 public long intake(Path path,String actor)throws Exception{
  String digest=fingerprint(path);db.setAutoCommit(false);
  try(var s=db.prepareStatement("INSERT INTO evidence(path,hash)VALUES(?,?)")){s.setString(1,path.toAbsolutePath().toString());s.setString(2,digest);s.executeUpdate();long id;try(var q=db.createStatement();var r=q.executeQuery("SELECT last_insert_rowid()")){r.next();id=r.getLong(1);}event(id,actor,"INTAKE");db.commit();return id;}catch(Exception e){db.rollback();throw e;}finally{db.setAutoCommit(true);}
 }
 public synchronized void event(long id,String actor,String action)throws Exception{
  String previous="GENESIS";try(var q=db.createStatement();var r=q.executeQuery("SELECT hash FROM events ORDER BY id DESC LIMIT 1")){if(r.next())previous=r.getString(1);}
  var bytes=new ByteArrayOutputStream();try(var out=new DataOutputStream(bytes)){out.writeLong(id);out.writeUTF(Instant.now().toString());out.writeUTF(actor);out.writeUTF(action);}
  String payload=Base64.getEncoder().encodeToString(bytes.toByteArray()),digest=hash((previous+":"+payload).getBytes(java.nio.charset.StandardCharsets.UTF_8));
  try(var s=db.prepareStatement("INSERT INTO events(evidence_id,payload,previous_hash,hash)VALUES(?,?,?,?)")){s.setLong(1,id);s.setString(2,payload);s.setString(3,previous);s.setString(4,digest);s.executeUpdate();}
 }
 public boolean verifyFile(long id)throws Exception{try(var q=db.prepareStatement("SELECT path,hash FROM evidence WHERE id=?")){q.setLong(1,id);try(var r=q.executeQuery()){if(!r.next())throw new IllegalArgumentException("unknown evidence");return fingerprint(Path.of(r.getString(1))).equals(r.getString(2));}}}
 public long verifyChain()throws Exception{String previous="GENESIS";try(var q=db.createStatement();var r=q.executeQuery("SELECT id,payload,previous_hash,hash FROM events ORDER BY id")){while(r.next()){String expected=hash((previous+":"+r.getString(2)).getBytes(java.nio.charset.StandardCharsets.UTF_8));if(!previous.equals(r.getString(3))||!expected.equals(r.getString(4)))return r.getLong(1);previous=r.getString(4);}}return 0;}
 public List<String> list()throws SQLException{var result=new ArrayList<String>();try(var q=db.createStatement();var r=q.executeQuery("SELECT id,path,hash FROM evidence")){while(r.next())result.add(r.getLong(1)+" | "+r.getString(2)+" | "+r.getString(3));}return result;}
 public void close()throws SQLException{db.close();}
}
