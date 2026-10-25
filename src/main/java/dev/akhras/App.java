package dev.akhras;
import javafx.application.Application;import javafx.scene.Scene;import javafx.scene.control.*;import javafx.scene.layout.*;import javafx.stage.*;
public class App extends Application{
 Tracker tracker;
 public void start(Stage stage)throws Exception{
  tracker=new Tracker("evidence.db");var list=new ListView<String>();var status=new Label();var actor=new TextField("custodian");var intake=new Button("Intake file");var verify=new Button("Verify selected file");var chain=new Button("Verify chain");var transfer=new Button("Record transfer");
  list.getItems().setAll(tracker.list());
  intake.setOnAction(e->{var file=new FileChooser().showOpenDialog(stage);if(file!=null)try{tracker.intake(file.toPath(),actor.getText());list.getItems().setAll(tracker.list());status.setText("Intake recorded");}catch(Exception ex){status.setText(ex.getMessage());}});
  verify.setOnAction(e->{try{long id=Long.parseLong(list.getSelectionModel().getSelectedItem().split(" \\| ")[0]);status.setText(tracker.verifyFile(id)?"Digest matches":"DIGEST CHANGED");}catch(Exception ex){status.setText(ex.getMessage());}});
  transfer.setOnAction(e->{try{long id=Long.parseLong(list.getSelectionModel().getSelectedItem().split(" \\| ")[0]);tracker.event(id,actor.getText(),"TRANSFER");status.setText("Transfer recorded");}catch(Exception ex){status.setText(ex.getMessage());}});
  chain.setOnAction(e->{try{long bad=tracker.verifyChain();status.setText(bad==0?"Chain verifies":"Broken event "+bad);}catch(Exception ex){status.setText(ex.getMessage());}});
  stage.setScene(new Scene(new VBox(10,new HBox(8,actor,intake,verify,transfer,chain),list,status),1000,500));stage.setTitle("Evidence custody");stage.show();
 }
 public void stop()throws Exception{tracker.close();}
 public static void main(String[]a){launch(a);}
}
