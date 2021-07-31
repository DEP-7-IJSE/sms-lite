package lk.ijse.dep7.sms_lite.controller;

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
    public ListView<String> lstContact;
    public TableView<StudentTM> tblStudents;
    Connection connection;

    public void initialize(){
        tblStudents.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudents.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<StudentTM, Button> lastCol = (TableColumn<StudentTM, Button>) tblStudents.getColumns().get(2);
        lastCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StudentTM, Button>, ObservableValue<Button>>() {
            @Override
            public ObservableValue<Button> call(TableColumn.CellDataFeatures<StudentTM, Button> param) {
                Button delete = new Button("Delete");
                return (ObservableValue<Button>) delete;
            }
        });

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sms_lite", "root", "mysql");
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM student");
            while (rst.next()){
                String id = rst.getString("id");
                String name = rst.getString("name");
                tblStudents.getItems().add(new StudentTM(id, name));
            }
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
    }

    public void btnSave_OnAction(ActionEvent actionEvent) {
        String id = txtID.getText();
        String name = txtName.getText();
        String phone = txtPhone.getText();
    }

    public void btnClear_OnAction(ActionEvent actionEvent) {
    }

    public void btnRemove_OnAction(ActionEvent actionEvent) {
    }
}
