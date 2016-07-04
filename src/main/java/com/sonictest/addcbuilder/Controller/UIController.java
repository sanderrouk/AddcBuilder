package com.sonictest.addcbuilder.Controller;

import com.sonictest.addcbuilder.Logic.ADDCBuilder;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class UIController implements Initializable{

    @FXML private Button uiCreateAddcButton;
    @FXML private TextField uiServerIpTextField;
    @FXML private TextField uiServerUsernameTextField;
    @FXML private TextField uiServerHostnameTextField;
    @FXML private TextField uiDomainUrlTextField;
    @FXML private TextField uiDomainNameTextField;
    @FXML private TextField uiNetGatewayTextField;
    @FXML private TextField uiNetNetmaskTextField;
    @FXML private TextField uiNetUpstreamTextField;
    @FXML private PasswordField uiServerPasswordTextField;
    @FXML private PasswordField uiDomainPasswordField;
    @FXML private TextArea uiConsole;
    @FXML private Service<Void> backgroundService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ADDCBuilder addcBuilder = new ADDCBuilder(uiConsole);


        //init ADDC Builder on button press
        uiCreateAddcButton.setOnMouseClicked((event -> {
            //Pull data from textfields
            String hostIp = uiServerIpTextField.getText();
            String hostUsername = uiServerUsernameTextField.getText();
            String hostPassword = uiServerPasswordTextField.getText();
            String hostname = uiServerHostnameTextField.getText();

            String domainURL = uiDomainUrlTextField.getText();
            String domainName = uiDomainNameTextField.getText();
            String domainPassword = uiDomainPasswordField.getText();

            String gateway = uiNetGatewayTextField.getText();
            String netmask = uiNetNetmaskTextField.getText();
            String upstream = uiNetUpstreamTextField.getText();



            backgroundService = new Service<Void>() {

                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            addcBuilder.buildAddc(hostIp,hostname, domainName, hostUsername, hostPassword, domainPassword, netmask, gateway, upstream, domainURL);
                            return null;
                        }
                    };
                }
            };

            backgroundService.start();
        }));

    }
}
