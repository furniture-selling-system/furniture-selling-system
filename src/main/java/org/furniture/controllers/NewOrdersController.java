package org.furniture.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.furniture.services.DBConnect;

import java.sql.SQLException;
import java.util.Collection;

public class NewOrdersController extends AbstractPageController {
    @FXML private TextField searchTextField;
    @FXML private ListView<String> ordersListView;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;
    @FXML private Button createButton;

    private Collection<String> customerName;

    @FXML
    public void initialize() throws SQLException {
        customerName = DBConnect.materialNameCollection();
        showOrderListView();
    }

    private void showOrderListView() {
        ordersListView.getItems().addAll(customerName);
        ordersListView.refresh();
    }

    @FXML
    private void searchTextFieldOnAction(KeyEvent e) {
        // TODO
    }

    @FXML
    private void ordersListViewOnAction (MouseEvent e) {
        // TODO
    }

    @FXML
    private void cancelButtonOnAction (ActionEvent e) {
        // TODO
    }

    @FXML
    private void confirmButtonOnAction (ActionEvent e) {
        // TODO
    }

    @FXML
    private void createButtonOnAction (ActionEvent e) {
        // TODO
    }
}
