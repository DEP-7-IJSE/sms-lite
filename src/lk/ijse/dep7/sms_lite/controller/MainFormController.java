package lk.ijse.dep7.sms_lite.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.dep7.sms_lite.TM.StudentTM;

import java.sql.*;
import java.util.Optional;

public class MainFormController {
    public TextField txtID;
    public TextField txtName;
    public TextField txtPhone;
    public Button btnSave;
    public Button btnNew;
    public ListView<String> lstContact;
    public TableView<StudentTM> tblStudents;
    Connection connection;
    PreparedStatement saveStudentStm;
    PreparedStatement saveContactStm;
    PreparedStatement searchStudentStm;
    PreparedStatement searchContactStm;
    PreparedStatement updateContactStm;
    int count = 0;

    public void initialize() {
        tblStudents.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudents.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<StudentTM, Button> lastCol = (TableColumn<StudentTM, Button>) tblStudents.getColumns().get(2);
        lastCol.setCellValueFactory(param -> {
            Button delete = new Button("Delete");
            delete.setOnAction(event -> {
                try {
                    Optional<ButtonType> deleteOption = new Alert(Alert.AlertType.WARNING, "Do you want to delete this student?", ButtonType.YES, ButtonType.NO).showAndWait();
                    if (deleteOption.get().equals(ButtonType.YES)) {
                        Statement stm = connection.createStatement();
                        stm.executeUpdate("DELETE FROM contact WHERE student_id = '" + param.getValue().getId() + "'");
                        int affectedRows = stm.executeUpdate("DELETE FROM student WHERE id = '" + param.getValue().getId() + "'");
                        if (affectedRows != 1) {
                            new Alert(Alert.AlertType.ERROR, "Deletion Failed").show();
                        }
                        new Alert(Alert.AlertType.INFORMATION, "Deleted").show();
                        tblStudents.getItems().remove(param.getValue());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            });
            return new ReadOnlyObjectWrapper<>(delete);
        });

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sms_lite", "root", "mysql");
            saveStudentStm = connection.prepareStatement("INSERT INTO student VALUES (?,?)");
            saveContactStm = connection.prepareStatement("INSERT INTO contact VALUES (?,?)");
            searchStudentStm = connection.prepareStatement("SELECT * FROM student WHERE id = ?");
            searchContactStm = connection.prepareStatement("SELECT * FROM contact WHERE student_id = ?");
            updateContactStm = connection.prepareStatement("UPDATE contact SET phone = ? WHERE student_id=? AND phone=?");
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM student");
            while (rst.next()) {
                String id = rst.getString("id");
                String name = rst.getString("name");
                tblStudents.getItems().add(new StudentTM(id, name));
                count = Integer.parseInt(id.split("S")[1]) + 1;
            }
            if (count == 0) count = 1;
            txtID.setText(String.format("S%03d", (count)));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        tblStudents.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null){
                String id = newValue.getId();
                txtID.setText(id);
                txtName.setText(newValue.getName());
                try {
                    Statement stm = connection.createStatement();
                    ResultSet rst = stm.executeQuery("SELECT * FROM contact WHERE student_id ='" + id + "'");
                    lstContact.getItems().clear();
                    while (rst.next()) {
                        String phone = rst.getString("phone");
                        lstContact.getItems().add(phone);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        lstContact.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                txtPhone.setText(newValue);
                btnSave.setText("Update");
            }
        });

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

        if (name.trim().isEmpty()){
            new Alert(Alert.AlertType.ERROR, "Invalid Name").show();
            txtName.requestFocus();
            return;
        }

        if (!phone.matches("(\\d{3}-\\d{7})")) {
            new Alert(Alert.AlertType.ERROR, "Invalid contact number").show();
            txtPhone.requestFocus();
            return;
        }

        try {
            searchStudentStm.setObject(1, id);
            ResultSet studentRst = searchStudentStm.executeQuery();
            boolean studentExist = false;
            while (studentRst.next()) {
                if (studentRst.getString("id").equals(id)) {
                    studentExist = true;
                }
            }
            if (!studentExist) {
                saveStudentStm.setObject(1, id);
                saveStudentStm.setObject(2, name);
                if (saveStudentStm.executeUpdate() != 1) {
                    new Alert(Alert.AlertType.ERROR, "Student save failed, try again").show();
                    return;
                }
            }

            searchContactStm.setObject(1, id);
            ResultSet rst = searchContactStm.executeQuery();
            while (rst.next()) {
                String student_id = rst.getString("student_id");
                String studentPhone = rst.getString("phone");
                if (student_id.equals(id) && studentPhone.equals(phone)) {
                    new Alert(Alert.AlertType.ERROR, "The information is here").show();
                    return;
                }
            }

            if (btnSave.getText().equalsIgnoreCase("Save")) {
                saveContactStm.setObject(1, id);
                saveContactStm.setObject(2, phone);

                if (saveContactStm.executeUpdate() != 1) {
                    new Alert(Alert.AlertType.ERROR, "save failed, try again").show();
                }
                new Alert(Alert.AlertType.INFORMATION, "Saved Successfully").show();
            }else {
                Optional<ButtonType> confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure that you want to update?", ButtonType.NO, ButtonType.YES).showAndWait();

                if (confirm.get().equals(ButtonType.YES)) {
                    updateContactStm.setObject(1,phone);
                    updateContactStm.setObject(2,id);
                    updateContactStm.setObject(3,lstContact.getSelectionModel().getSelectedItem());

                    if (updateContactStm.executeUpdate() != 1){
                        new Alert(Alert.AlertType.ERROR,"Update failure, try again").show();
                    }
                    new Alert(Alert.AlertType.INFORMATION,"Saved Successfully").show();
                    lstContact.getItems().remove(lstContact.getSelectionModel().getSelectedItem());
                }
            }

            if (!studentExist) {
                tblStudents.getItems().add(new StudentTM(id, name));
                txtID.setText(String.format("S%03d", (++count)));
            }else {
                txtID.setText(String.format("S%03d", (count)));
                txtName.requestFocus();
            }
            lstContact.getItems().add(phone);
            txtName.clear();
            txtPhone.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void btnClear_OnAction(ActionEvent actionEvent) {
        deleteContacts(true);
    }

    public void btnRemove_OnAction(ActionEvent actionEvent) {
        deleteContacts(false);
    }

    private void deleteContacts(boolean all) {
        try {
            int deletedRaws;
            Optional<ButtonType> confirm = new Alert(Alert.AlertType.WARNING, "Are you sure you want to delete contact(s)?", ButtonType.YES, ButtonType.NO).showAndWait();
            if (confirm.get().equals(ButtonType.YES)) {
                if (all) {
                    deletedRaws = connection.createStatement().executeUpdate("DELETE FROM contact WHERE student_id='" + txtID.getText() + "'");
                    lstContact.getItems().clear();
                    txtName.clear();
                } else {
                    deletedRaws = connection.createStatement().executeUpdate("DELETE FROM contact WHERE student_id='" + txtID.getText() + "' AND phone = '" + lstContact.getSelectionModel().getSelectedItem() + "'");
                    lstContact.getItems().remove(lstContact.getSelectionModel().getSelectedItem());
                }
                if (deletedRaws >= 1) {
                    new Alert(Alert.AlertType.INFORMATION, "Deleted").show();
                    txtPhone.clear();
                    txtID.setText(String.format("S%03d", (count)));
                    return;
                }
                new Alert(Alert.AlertType.ERROR,"Deletion Failed").show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void btnNew_OnAction(ActionEvent actionEvent) {
        btnSave.setText("Save");
        txtName.clear();
        txtPhone.clear();
        lstContact.getItems().clear();
        txtID.setText(String.format("S%03d", (count)));
    }
}
