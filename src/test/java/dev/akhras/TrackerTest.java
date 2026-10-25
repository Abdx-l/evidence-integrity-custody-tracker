package dev.akhras;
import org.junit.jupiter.api.*;import org.junit.jupiter.api.io.TempDir;import java.nio.file.*;import static org.junit.jupiter.api.Assertions.*;
class TrackerTest{
 @TempDir Path dir;
 @Test void tampering()throws Exception{
  Path f=dir.resolve("sample");Files.writeString(f,"original");
  try(var t=new Tracker(dir.resolve("test.db").toString())){
   long id=t.intake(f,"owner");assertTrue(t.verifyFile(id));assertEquals(0,t.verifyChain());
   Files.writeString(f,"changed");assertFalse(t.verifyFile(id));
   t.event(id,"next","TRANSFER");t.db.createStatement().execute("UPDATE events SET payload='altered' WHERE id=1");assertEquals(1,t.verifyChain());
  }
 }
}
