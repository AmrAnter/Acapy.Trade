/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screens.hr;
 
import acapy.trade.AcapyTrade;
import assets.classes.AlertDialogs;
import assets.classes.statics;
import static assets.classes.statics.*; 
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import screens.hr.assets.Devices;
import screens.hr.assets.Employee;

/**
 * FXML Controller class
 *
 * @author AHMED
 */
public class HrScreenDeviceOperationsController implements Initializable {

    @FXML
    private Button syncTime;
    @FXML
    private Button powerOff;
    @FXML
    private Button restart;
    @FXML
    private Button clearAdmins;
    @FXML
    private Button clearAttendance;
    @FXML
    private Button deleteAllData;
    @FXML
    private ComboBox<Devices> devices;
    Preferences prefs;
    @FXML
    private Button setAdmin;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        //Background work                       
                        final CountDownLatch latch = new CountDownLatch(1);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    prefs = Preferences.userNodeForPackage(AcapyTrade.class);
                                    devices.setItems(Devices.getData());
                                    devices.setConverter(new StringConverter<Devices>() {
                                        @Override
                                        public String toString(Devices patient) {
                                            return patient.getName();
                                        }

                                        @Override
                                        public Devices fromString(String string) {
                                            return null;
                                        }
                                    });
                                    devices.setCellFactory(cell -> new ListCell<Devices>() {

                                        // Create our layout here to be reused for each ListCell
                                        GridPane gridPane = new GridPane();
                                        Label lblid = new Label();
                                        Label lblName = new Label();
                                        Label lblIP = new Label();
                                        Label lblPort = new Label();

                                        // Static block to configure our layout
                                        {
                                            // Ensure all our column widths are constant
                                            gridPane.getColumnConstraints().addAll(
                                                    new ColumnConstraints(100, 100, 100), new ColumnConstraints(100, 100, 100), new ColumnConstraints(100, 100, 100),
                                                    new ColumnConstraints(100, 100, 100)
                                            );

                                            gridPane.add(lblid, 0, 1);
                                            gridPane.add(lblName, 1, 1);
                                            gridPane.add(lblIP, 2, 1);
                                            gridPane.add(lblPort, 3, 1);

                                        }

                                        // We override the updateItem() method in order to provide our own layout for this Cell's graphicProperty
                                        @Override
                                        protected void updateItem(Devices person, boolean empty) {
                                            super.updateItem(person, empty);

                                            if (!empty && person != null) {

                                                // Update our Labels
                                                lblid.setText("م: " + Integer.toString(person.getId()));
                                                lblName.setText("الاسم: " + person.getName());
                                                lblIP.setText("ip: " + person.getIp());
                                                lblPort.setText("port: " + person.getPort());
                                                // Set this ListCell's graphicProperty to display our GridPane
                                                setGraphic(gridPane);
                                            } else {
                                                // Nothing to display here
                                                setGraphic(null);
                                            }
                                        }
                                    });
                                } catch (Exception ex) {
                                    AlertDialogs.showErrors(ex);
                                } finally {
                                    latch.countDown();
                                }
                            }

                        });
                        latch.await();

                        return null;
                    }
                };

            }

            @Override
            protected void succeeded() {

                super.succeeded();
            }
        };
        service.start();
    }

    @FXML
    private void syncTime(ActionEvent event) {
        try {
            if (devices.getSelectionModel().getSelectedIndex() == -1) {
                AlertDialogs.showError("اختار الجهاز اولا");
            } else {
                AlertDialogs.showmessage("سيتم التنفيذ قد يتسبب فى توقف مؤقت للبرنامج");
                Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                //Background work                       
                                final CountDownLatch latch = new CountDownLatch(1);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {

                                            try {
                                                Process p = Runtime.getRuntime().exec(prefs.get(statics.PYTHON_PATH, statics.PYTHON_PATH_DEFAULT) + statics.syncTime);
                                                p.waitFor();
                                                BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                                BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                                String line;
                                                while ((line = bri.readLine()) != null) {
                                                    System.out.println(line);
                                                }
                                                bri.close();
                                                while ((line = bre.readLine()) != null) {
                                                    System.out.println(line);

                                                }
                                                bre.close();
                                                p.waitFor();
                                                System.out.println("Done.");

                                                p.destroy();

                                            } catch (Exception ex) {
                                                AlertDialogs.showErrors(ex);
                                            } finally {
                                                latch.countDown();
                                            }
                                        } catch (Exception ex) {
                                            AlertDialogs.showErrors(ex);
                                        } finally {
                                            latch.countDown();
                                        }
                                    }

                                });
                                latch.await();

                                return null;
                            }

                        };

                    }

                    @Override
                    protected void succeeded() {
                        AlertDialogs.showmessage("تم");
                        super.succeeded();
                    }
                };
                service.start();
            }

        } catch (Exception ex) {
            AlertDialogs.showErrors(ex);
        }
    }

    @FXML
    private void powerOff(ActionEvent event) {
        try {
            if (devices.getSelectionModel().getSelectedIndex() == -1) {
                AlertDialogs.showError("اختار الجهاز اولا");
            } else {
                AlertDialogs.showmessage("سيتم التنفيذ قد يتسبب فى توقف مؤقت للبرنامج");
                Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                //Background work                       
                                final CountDownLatch latch = new CountDownLatch(1);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {

                                            try {
                                                Process p = Runtime.getRuntime().exec(prefs.get(statics.PYTHON_PATH, statics.PYTHON_PATH_DEFAULT) + statics.powerOff);
                                                p.waitFor();
                                                BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                                BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                                String line;
                                                while ((line = bri.readLine()) != null) {
                                                    System.out.println(line);
                                                }
                                                bri.close();
                                                while ((line = bre.readLine()) != null) {
                                                    System.out.println(line);

                                                }
                                                bre.close();
                                                p.waitFor();
                                                System.out.println("Done.");

                                                p.destroy();

                                            } catch (Exception ex) {
                                                AlertDialogs.showErrors(ex);
                                            } finally {
                                                latch.countDown();
                                            }
                                        } catch (Exception ex) {
                                            AlertDialogs.showErrors(ex);
                                        } finally {
                                            latch.countDown();
                                        }
                                    }

                                });
                                latch.await();

                                return null;
                            }

                        };

                    }

                    @Override
                    protected void succeeded() {
                        syncTime(null);
                        super.succeeded();
                    }
                };
                service.start();
            }

        } catch (Exception ex) {
            AlertDialogs.showErrors(ex);
        }
    }

    @FXML
    private void restart(ActionEvent event) {
        try {
            if (devices.getSelectionModel().getSelectedIndex() == -1) {
                AlertDialogs.showError("اختار الجهاز اولا");
            } else {
                AlertDialogs.showmessage("سيتم التنفيذ قد يتسبب فى توقف مؤقت للبرنامج");
                Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                //Background work                       
                                final CountDownLatch latch = new CountDownLatch(1);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {

                                            try {
                                                Process p = Runtime.getRuntime().exec(prefs.get(statics.PYTHON_PATH, statics.PYTHON_PATH_DEFAULT) + statics.restart);
                                                p.waitFor();
                                                BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                                BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                                String line;
                                                while ((line = bri.readLine()) != null) {
                                                    System.out.println(line);
                                                }
                                                bri.close();
                                                while ((line = bre.readLine()) != null) {
                                                    System.out.println(line);

                                                }
                                                bre.close();
                                                p.waitFor();
                                                System.out.println("Done.");

                                                p.destroy();

                                            } catch (Exception ex) {
                                                AlertDialogs.showErrors(ex);
                                            } finally {
                                                latch.countDown();
                                            }
                                        } catch (Exception ex) {
                                            AlertDialogs.showErrors(ex);
                                        } finally {
                                            latch.countDown();
                                        }
                                    }

                                });
                                latch.await();

                                return null;
                            }

                        };

                    }

                    @Override
                    protected void succeeded() {
                        syncTime(null);
                        super.succeeded();
                    }
                };
                service.start();
            }

        } catch (Exception ex) {
            AlertDialogs.showErrors(ex);
        }
    }

    @FXML
    private void clearAdmins(ActionEvent event) {
        try {
            if (devices.getSelectionModel().getSelectedIndex() == -1) {
                AlertDialogs.showError("اختار الجهاز اولا");
            } else {
                AlertDialogs.showmessage("سيتم التنفيذ قد يتسبب فى توقف مؤقت للبرنامج");
                Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                //Background work                       
                                final CountDownLatch latch = new CountDownLatch(1);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {

                                            try {
                                                Process p = Runtime.getRuntime().exec(prefs.get(statics.PYTHON_PATH, statics.PYTHON_PATH_DEFAULT) + statics.deleteAdmins);
                                                p.waitFor();
                                                BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                                BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                                String line;
                                                while ((line = bri.readLine()) != null) {
                                                    System.out.println(line);
                                                }
                                                bri.close();
                                                while ((line = bre.readLine()) != null) {
                                                    System.out.println(line);

                                                }
                                                bre.close();
                                                p.waitFor();
                                                System.out.println("Done.");

                                                p.destroy();

                                            } catch (Exception ex) {
                                                AlertDialogs.showErrors(ex);
                                            } finally {
                                                latch.countDown();
                                            }
                                        } catch (Exception ex) {
                                            AlertDialogs.showErrors(ex);
                                        } finally {
                                            latch.countDown();
                                        }
                                    }

                                });
                                latch.await();

                                return null;
                            }

                        };

                    }

                    @Override
                    protected void succeeded() {
                        AlertDialogs.showmessage("تم");
                        super.succeeded();
                    }
                };
                service.start();
            }

        } catch (Exception ex) {
            AlertDialogs.showErrors(ex);
        }
    }

    @FXML
    private void clearAttendance(ActionEvent event) {
        try {
            if (devices.getSelectionModel().getSelectedIndex() == -1) {
                AlertDialogs.showError("اختار الجهاز اولا");
            } else {
                AlertDialogs.showmessage("سيتم التنفيذ قد يتسبب فى توقف مؤقت للبرنامج");
                Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                final CountDownLatch latch = new CountDownLatch(1);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {

                                            try {
                                                Process p = Runtime.getRuntime().exec(prefs.get(statics.PYTHON_PATH, statics.PYTHON_PATH_DEFAULT) + statics.clearAttendance);
                                                p.waitFor();
                                                BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                                BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                                String line;
                                                while ((line = bri.readLine()) != null) {
                                                    System.out.println(line);
                                                }
                                                bri.close();
                                                while ((line = bre.readLine()) != null) {
                                                    System.out.println(line);

                                                }
                                                bre.close();
                                                p.waitFor();
                                                System.out.println("Done.");

                                                p.destroy();

                                            } catch (Exception ex) {
                                                AlertDialogs.showErrors(ex);
                                            } finally {
                                                latch.countDown();
                                            }
                                        } catch (Exception ex) {
                                            AlertDialogs.showErrors(ex);
                                        } finally {
                                            latch.countDown();
                                        }
                                    }

                                });
                                latch.await();

                                return null;
                            }

                        };

                    }

                    @Override
                    protected void succeeded() {
                        AlertDialogs.showmessage("تم");
                        super.succeeded();
                    }
                };
                service.start();
            }
        } catch (Exception ex) {
            AlertDialogs.showErrors(ex);
        }
    }

    @FXML
    private void deleteAllData(ActionEvent event) {
        try {
            if (devices.getSelectionModel().getSelectedIndex() == -1) {
                AlertDialogs.showError("اختار الجهاز اولا");
            } else {
                AlertDialogs.showmessage("سيتم التنفيذ قد يتسبب فى توقف مؤقت للبرنامج");
                Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                //Background work                       
                                final CountDownLatch latch = new CountDownLatch(1);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {

                                            try {
                                                Process p = Runtime.getRuntime().exec(prefs.get(statics.PYTHON_PATH, statics.PYTHON_PATH_DEFAULT) + clearData);
                                                p.waitFor();
                                                BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                                BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                                String line;
                                                while ((line = bri.readLine()) != null) {
                                                    System.out.println(line);
                                                }
                                                bri.close();
                                                while ((line = bre.readLine()) != null) {
                                                    System.out.println(line);

                                                }
                                                bre.close();
                                                p.waitFor();
                                                System.out.println("Done.");

                                                p.destroy();

                                            } catch (Exception ex) {
                                                AlertDialogs.showErrors(ex);
                                            } finally {
                                                latch.countDown();
                                            }
                                        } catch (Exception ex) {
                                            AlertDialogs.showErrors(ex);
                                        } finally {
                                            latch.countDown();
                                        }
                                    }

                                });
                                latch.await();

                                return null;
                            }

                        };

                    }

                    @Override
                    protected void succeeded() {
                        AlertDialogs.showmessage("تم");
                        super.succeeded();
                    }
                };
                service.start();
            }
        } catch (Exception ex) {
            AlertDialogs.showErrors(ex);
        }
    }

    @FXML
    private void updateWorkingDevice(ActionEvent event) {
        try {
            db.get.getReportCon().createStatement().execute("REPLACE INTO `att_target_devices`(`device_id`) select `att_machines`.`id` from `att_machines` where `att_machines`.`name`='" + devices.getSelectionModel().getSelectedItem().getName() + "'");
        } catch (Exception ex) {
            AlertDialogs.showErrors(ex);
        }
    }

    @FXML
    private void setAdmin(ActionEvent event) {
        try {
            ObservableList<Employee> data = Employee.getData();
            List<String> choices = new ArrayList<>();
            for (Employee a : data) {
                choices.add("id: " + a.getId() + "-" + "name: " + a.getName());
            }
            Employee c = data.get(0);
            ChoiceDialog<String> dialog = new ChoiceDialog<>("id: " + c.getId() + "-" + "name: " + c.getName(), choices);
            dialog.setTitle("Choice Employee");
            dialog.setHeaderText("اختار الموظف");
            dialog.setContentText("اختار اسم الموضف ");
            dialog.setGraphic(new ImageView(this.getClass().getResource("/assets/icons/icons8_hdd_80px.png").toString()));
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String a = result.get();
                String id = "";
                String name = "";
                String[] split = a.split("-");
                id = split[0].split(":")[1].replaceAll(" ", "");
                name = split[1].split(":")[1].replaceAll(" ", "");
                if (db.get.getReportCon().createStatement().execute("UPDATE `att_device_users` SET `privileges`='Admin' WHERE `userId`='"+id+"' and `name`='"+name+"'")) {
                    try {
                        if (devices.getSelectionModel().getSelectedIndex() == -1) {
                            AlertDialogs.showError("اختار الجهاز اولا");
                        } else {
                            AlertDialogs.showmessage("سيتم التنفيذ قد يتسبب فى توقف مؤقت للبرنامج");
                            Service<Void> service = new Service<Void>() {
                                @Override
                                protected Task<Void> createTask() {
                                    return new Task<Void>() {
                                        @Override
                                        protected Void call() throws Exception {
                                            //Background work
                                            final CountDownLatch latch = new CountDownLatch(1);
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {

                                                        try {
                                                            Process p = Runtime.getRuntime().exec(prefs.get(statics.PYTHON_PATH, statics.PYTHON_PATH_DEFAULT) + uploadAdmins);
                                                            p.waitFor();
                                                            BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                                            BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                                            String line;
                                                            while ((line = bri.readLine()) != null) {
                                                                System.out.println(line);
                                                            }
                                                            bri.close();
                                                            while ((line = bre.readLine()) != null) {
                                                                System.out.println(line);

                                                            }
                                                            bre.close();
                                                            p.waitFor();
                                                            System.out.println("Done.");

                                                            p.destroy();

                                                        } catch (Exception ex) {
                                                            AlertDialogs.showErrors(ex);
                                                        } finally {
                                                            latch.countDown();
                                                        }
                                                    } catch (Exception ex) {
                                                        AlertDialogs.showErrors(ex);
                                                    } finally {
                                                        latch.countDown();
                                                    }
                                                }

                                            });
                                            latch.await();

                                            return null;
                                        }

                                    };

                                }

                                @Override
                                protected void succeeded() {
                                    AlertDialogs.showmessage("تم");
                                    super.succeeded();
                                }
                            };
                            service.start();
                        }
                    } catch (Exception ex) {
                        AlertDialogs.showErrors(ex);
                    }
                }
            }
        } catch (Exception ex) {
            AlertDialogs.showErrors(ex);
        }
    }
}
