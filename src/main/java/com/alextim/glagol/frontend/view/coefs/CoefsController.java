package com.alextim.glagol.frontend.view.coefs;

import com.alextim.glagol.service.CoefService.CoefsMeasurement;
import com.alextim.glagol.service.message.CommandMessages;
import javafx.application.Platform;

import java.util.List;

import static com.alextim.glagol.service.protocol.Parameter.*;

public class CoefsController extends CoefsControllerInitializer {

    @Override
    protected void startCoefCalculated(int measTime, float realMeasData, int range) {
        rootController.startCoefCalculated(measTime, realMeasData, range);
    }

    @Override
    protected void stopCoefCalculated() {
        rootController.stopCoefCalculated();
    }

    @Override
    protected void setCoef(int range, float[] coefs) {
        if(range == 1) {
            if (areYouSure(BD_BG_SI29G_COEFF_1)) {
                rootController.sendDetectorCommand(List.of(
                        new CommandMessages.SetParamCommand(BD_BG_SI29G_COEFF_1, coefs[0]),
                        new CommandMessages.SetParamCommand(BD_BG_SI29G_COEFF_2, coefs[1]),
                        new CommandMessages.SetParamCommand(BD_BG_SI29G_COEFF_3, coefs[2]),
                        new CommandMessages.SetParamCommand(BD_BG_SI29G_COEFF_4, coefs[3])
                ));
            }
        } else if(range == 2) {
            if (areYouSure(BD_BG_SBM21_COEFF_1)) {
                rootController.sendDetectorCommand(List.of(
                        new CommandMessages.SetParamCommand(BD_BG_SBM21_COEFF_1, coefs[0]),
                        new CommandMessages.SetParamCommand(BD_BG_SBM21_COEFF_2, coefs[1])
                ));
            }
        } else if(range == 3) {
            if (areYouSure(BD_BG_SI38G_COEFF_1)) {
                rootController.sendDetectorCommand(List.of(
                        new CommandMessages.SetParamCommand(BD_BG_SI38G_COEFF_1, coefs[0]),
                        new CommandMessages.SetParamCommand(BD_BG_SI38G_COEFF_2, coefs[1])
                ));
            }
        }
    }

    public void showCoefMeas(CoefsMeasurement coefsMeasurement) {
        Platform.runLater(() -> {
            updateTable(coefsMeasurement);
            setProgress(coefsMeasurement.progress);
        });
    }
}
