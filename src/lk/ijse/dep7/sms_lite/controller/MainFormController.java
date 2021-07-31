package lk.ijse.dep7.sms_lite.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import lk.ijse.dep7.sms_lite.TM.StudentTM;

import java.sql.*;

public class MainFormController {
    public TextField txtID;
    public TextField txtName;
    public TextField txtPhone;
    public Button btnSave;
    public ListView<String> lstContact;
    public TableView<StudentTM> tblStudents;
    Connection connection;
    PreparedStatement saveStudentStm;
    PreparedStatement saveContactStm;
    int count=0;

    public void initialize(){
        tblStudents.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudents.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<StudentTM, Button> lastCol = (TableColumn<StudentTM, Button>) tblStudents.getColumns().get(2);
        lastCol.setCellValueFactory(param -> {
            Button delete = new Button("Delete");
            return new ReadOnlyObjectWrapper<>(delete);
        });

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sms_lite", "root", "mysql");
            saveStudentStm = connection.prepareStatement("INSERT INTO student VALUES (?,?)");
            saveContactStm = connection.prepareStatement("INSERT INTO contact VALUES (?,?)");
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM student");
            while (rst.next()){
                String id = rst.getString("id");
                String name = rst.getString("name");
                tblStudents.getItems().add(new StudentTM(id, name));
                count++;
            }
            txtID.setText(String.format("S%03d",(count+1)));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
    }

    public void txtPhone_OnAction(ActionEvent actionEvent) {
        btnSave.fire();
    }

    public void btnSave_OnAction(ActionEvent actionEvent) {
        String id = txtID.getText();
        String name = txtName.getText();
        String phone = txtPhone.getText();

        if (!phone.matches("(\\d{3}-\\d{7})")){
            new Alert(Alert.AlertType.ERROR, "Invalid contact number").show();
            return;
        }

        try {
            saveStudentStm.setObject(1,id);
            saveStudentStm.setObject(2,name);
            if (saveStudentStm.executeUpdate() != 1){
                new Alert(Alert.AlertType.ERROR, "Student save failed, try again").show();
                return;
            }
            saveContactStm.setObject(1,id);
            saveContactStm.setObject(2,phone);
            if (saveContactStm.executeUpdate() != 1){
                new Alert(Alert.AlertType.ERROR, "Student save failed, try again").show();
            }
            new Alert(Alert.AlertType.INFORMATION,"Saved Successfully").show();

            tblStudents.getItems().add(new StudentTM(id,name));
            lstContact.getItems().add(phone);
            txtID.setText(String.format("S%03d",(++count)));
            txtName.clear();
            txtPhone.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void btnClear_OnAction(ActionEvent actionEvent) {
    }

    public void btnRemove_OnAction(ActionEvent actionEvent) {
    }
}
