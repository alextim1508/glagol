package com.alextim.glagol.frontend.view.management;

import com.alextim.glagol.service.message.CommandMessages.GetParamCommand;
import com.alextim.glagol.service.message.CommandMessages.RestartCommand;
import com.alextim.glagol.service.message.CommandMessages.SetParamCommand;
import com.alextim.glagol.service.protocol.Parameter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.alextim.glagol.context.Property.DATE_BUILD;
import static com.alextim.glagol.context.Property.SOFTWARE_VERSION;
import static com.alextim.glagol.service.protocol.Parameter.*;

@Slf4j
public class ManagementController extends ManagementControllerInitializer {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        setSoftwareVersion(SOFTWARE_VERSION, DATE_BUILD);
    }

    @FXML
    void setDeadTimeOn(ActionEvent event) {
        Parameter parameter = BD_BG_RANGE1_DEAD_TIME;
        if (areYouSure(parameter)) {
            try {
                log.info("Setting dead time parameters");

                BigDecimal deadTime1BD = new BigDecimal(this.deadTime1.getText().trim()).setScale(6, RoundingMode.HALF_UP);
                BigDecimal deadTime2BD = new BigDecimal(this.deadTime2.getText().trim()).setScale(6, RoundingMode.HALF_UP);
                BigDecimal deadTime3BD = new BigDecimal(this.deadTime3.getText().trim()).setScale(6, RoundingMode.HALF_UP);

                float deadTime1 = deadTime1BD.floatValue();
                float deadTime2 = deadTime2BD.floatValue();
                float deadTime3 = deadTime3BD.floatValue();
                log.debug("Parsed dead times: 1={}, 2={}, 3={}", deadTime1, deadTime2, deadTime3);

                var commands = Arrays.asList(
                        new SetParamCommand(BD_BG_RANGE1_DEAD_TIME, deadTime1),
                        new SetParamCommand(BD_BG_RANGE2_DEAD_TIME, deadTime2),
                        new SetParamCommand(BD_BG_RANGE3_DEAD_TIME, deadTime3)
                );
                log.debug("Sending SetParamCommand batch for dead times: {}", commands);

                rootController.sendDetectorCommand(commands);

                log.info("Dead time parameters sent successfully");
            } catch (NumberFormatException e) {
                log.error("Failed to parse dead time values from UI fields", e);
                showParsingErrorDialog(parameter);
            } catch (Exception e) {
                log.error("Unexpected error during setDeadTimeOn", e);
                showParsingErrorDialog(parameter);
            }
        }
    }

    @SneakyThrows
    @FXML
    void getDeadTimeOn(ActionEvent event) {
        log.info("Requesting dead time parameters");

        deadTime1.setText("-");
        deadTime2.setText("-");
        deadTime3.setText("-");

        var commands = Arrays.asList(
                new GetParamCommand(BD_BG_RANGE1_DEAD_TIME),
                new GetParamCommand(BD_BG_RANGE2_DEAD_TIME),
                new GetParamCommand(BD_BG_RANGE3_DEAD_TIME)
        );
        log.debug("Sending GetParamCommand batch for dead times: {}", commands);

        rootController.sendDetectorCommand(commands);
    }

    @FXML
    void setCorrCoefOn1(ActionEvent event) {
        Parameter parameter = BD_BG_SI29G_COEFF_1;
        if (areYouSure(parameter)) {
            try {
                log.info("Setting correction coefficients for SI29G");

                BigDecimal counter1BD = new BigDecimal(this.counterCoef11.getText().trim()).setScale(6, RoundingMode.HALF_UP);
                BigDecimal counter2BD = new BigDecimal(this.counterCoef12.getText().trim()).setScale(6, RoundingMode.HALF_UP);
                BigDecimal counter3BD = new BigDecimal(this.counterCoef13.getText().trim()).setScale(6, RoundingMode.HALF_UP);
                BigDecimal counter4BD = new BigDecimal(this.counterCoef14.getText().trim()).setScale(6, RoundingMode.HALF_UP);

                float counter1 = counter1BD.floatValue();
                float counter2 = counter2BD.floatValue();
                float counter3 = counter3BD.floatValue();
                float counter4 = counter4BD.floatValue();
                log.debug("Parsed SI29G coefficients: 1={}, 2={}, 3={}, 4={}", counter1, counter2, counter3, counter4);

                var commands = Arrays.asList(
                        new SetParamCommand(BD_BG_SI29G_COEFF_1, counter1),
                        new SetParamCommand(BD_BG_SI29G_COEFF_2, counter2),
                        new SetParamCommand(BD_BG_SI29G_COEFF_3, counter3),
                        new SetParamCommand(BD_BG_SI29G_COEFF_4, counter4)
                );
                log.debug("Sending SetParamCommand batch for SI29G coefficients: {}", commands);

                rootController.sendDetectorCommand(commands);

                log.info("SI29G correction coefficients sent successfully");
            } catch (NumberFormatException e) {
                log.error("Failed to parse SI29G coefficient values from UI fields", e);
                showParsingErrorDialog(parameter);
            } catch (Exception e) {
                log.error("Unexpected error during setCorrCoefOn1", e);
                showParsingErrorDialog(parameter);
            }
        }
    }

    @FXML
    void getCorrCoefOn1(ActionEvent event) {
        log.info("Requesting correction coefficients for SI29G");

        counterCoef11.setText("-");
        counterCoef12.setText("-");
        counterCoef13.setText("-");
        counterCoef14.setText("-");

        var commands = Arrays.asList(
                new GetParamCommand(BD_BG_SI29G_COEFF_1),
                new GetParamCommand(BD_BG_SI29G_COEFF_2),
                new GetParamCommand(BD_BG_SI29G_COEFF_3),
                new GetParamCommand(BD_BG_SI29G_COEFF_4)
        );
        log.debug("Sending GetParamCommand batch for SI29G coefficients: {}", commands);

        rootController.sendDetectorCommand(commands);
    }

    @FXML
    void setCorrCoefOn2(ActionEvent event) {
        Parameter parameter = BD_BG_SBM21_COEFF_1;
        if (areYouSure(parameter)) {
            try {
                log.info("Setting correction coefficients for SBM21");

                BigDecimal counter1BD = new BigDecimal(this.counterCoef21.getText().trim()).setScale(6, RoundingMode.HALF_UP);
                BigDecimal counter2BD = new BigDecimal(this.counterCoef22.getText().trim()).setScale(6, RoundingMode.HALF_UP);

                float counter1 = counter1BD.floatValue();
                float counter2 = counter2BD.floatValue();
                log.debug("Parsed SBM21 coefficients: 1={}, 2={}", counter1, counter2);

                var commands = Arrays.asList(
                        new SetParamCommand(BD_BG_SBM21_COEFF_1, counter1),
                        new SetParamCommand(BD_BG_SBM21_COEFF_2, counter2)
                );
                log.debug("Sending SetParamCommand batch for SBM21 coefficients: {}", commands);

                rootController.sendDetectorCommand(commands);

                log.info("SBM21 correction coefficients sent successfully");
            } catch (NumberFormatException e) {
                log.error("Failed to parse SBM21 coefficient values from UI fields", e);
                showParsingErrorDialog(parameter);
            } catch (Exception e) {
                log.error("Unexpected error during setCorrCoefOn2", e);
                showParsingErrorDialog(parameter);
            }
        }
    }

    @FXML
    void getCorrCoefOn2(ActionEvent event) {
        log.info("Requesting correction coefficients for SBM21");

        counterCoef21.setText("-");
        counterCoef22.setText("-");

        var commands = Arrays.asList(
                new GetParamCommand(BD_BG_SBM21_COEFF_1),
                new GetParamCommand(BD_BG_SBM21_COEFF_2)
        );
        log.debug("Sending GetParamCommand batch for SBM21 coefficients: {}", commands);

        rootController.sendDetectorCommand(commands);
    }

    @FXML
    void setCorrCoefOn3(ActionEvent event) {
        Parameter parameter = BD_BG_SI38G_COEFF_1;
        if (areYouSure(parameter)) {
            try {
                log.info("Setting correction coefficients for SI38G");

                BigDecimal counter1BD = new BigDecimal(this.counterCoef31.getText().trim()).setScale(6, RoundingMode.HALF_UP);
                BigDecimal counter2BD = new BigDecimal(this.counterCoef32.getText().trim()).setScale(6, RoundingMode.HALF_UP);

                float counter1 = counter1BD.floatValue();
                float counter2 = counter2BD.floatValue();
                log.debug("Parsed SI38G coefficients: 1={}, 2={}", counter1, counter2);

                var commands = Arrays.asList(
                        new SetParamCommand(BD_BG_SI38G_COEFF_1, counter1),
                        new SetParamCommand(BD_BG_SI38G_COEFF_2, counter2)
                );
                log.debug("Sending SetParamCommand batch for SI38G coefficients: {}", commands);

                rootController.sendDetectorCommand(commands);

                log.info("SI38G correction coefficients sent successfully");
            } catch (NumberFormatException e) {
                log.error("Failed to parse SI38G coefficient values from UI fields", e);
                showParsingErrorDialog(parameter);
            } catch (Exception e) {
                log.error("Unexpected error during setCorrCoefOn3", e);
                showParsingErrorDialog(parameter);
            }
        }
    }

    @FXML
    void getCorrCoefOn3(ActionEvent event) {
        log.info("Requesting correction coefficients for SI38G");

        counterCoef31.setText("-");
        counterCoef32.setText("-");

        var commands = Arrays.asList(
                new GetParamCommand(BD_BG_SI38G_COEFF_1),
                new GetParamCommand(BD_BG_SI38G_COEFF_2)
        );
        log.debug("Sending GetParamCommand batch for SI38G coefficients: {}", commands);

        rootController.sendDetectorCommand(commands);
    }

    @FXML
    void restartOn(ActionEvent event) {
        if (areYouSureDetectorRestart()) {
            log.info("Sending detector restart command");
            rootController.sendDetectorCommand(new RestartCommand());
            log.debug("Restart command sent");
        }
    }
}