package com.sonictest.addcbuilder.Controller;

import com.sonictest.addcbuilder.Logic.ADDCBuilder;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class UIController implements Initializable{

    @FXML private Button uiCreateAddcButton;
    @FXML private Button uiHelpButton;
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

        //Tooltips
        uiServerIpTextField.setTooltip(new Tooltip("Target server ip, usually local, needs to support SSH."));
        uiServerUsernameTextField.setTooltip(new Tooltip("User with rights to run sudo commands."));
        uiServerPasswordTextField.setTooltip(new Tooltip("Password to user with rights to sudo commands."));
        uiServerHostnameTextField.setTooltip(new Tooltip("Server hostname e.g. dc1."));
        uiDomainUrlTextField.setTooltip(new Tooltip("Domain URL, e.g. phantomarts.xyz."));
        uiDomainNameTextField.setTooltip(new Tooltip("Domain name, e.g. CorpDom."));
        uiDomainPasswordField.setTooltip(new Tooltip("Domain administrator account password."));
        uiNetGatewayTextField.setTooltip(new Tooltip("Server gateway, e.g. 192.168.0.1."));
        uiNetNetmaskTextField.setTooltip(new Tooltip("Server netmask, e.g. 255.255.255.0 not /24."));
        uiNetUpstreamTextField.setTooltip(new Tooltip("This needs to be the server which is going to be handling the " +
                "upstream DNS, if you don't have one use 8.8.8.8(Google DNS)."));

        ADDCBuilder addcBuilder = new ADDCBuilder(uiConsole);

        uiHelpButton.setOnMouseClicked((event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("For additional help visit:");
            alert.setContentText("http://addcbuilder.phantomarts.xyz or \n " +
                    "http://www.phantomarts.xyz/addcbuilder");

            alert.setTitle("Info");
            alert.showAndWait();
        }));


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
