package org.tyszecki.rozkladpkp;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey = "dFlJOVYyS1hYbENUWEVmQnE5azlKNFE6MQ")

public class RozkladPKPApplication extends Application {
	@Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        //ACRA.init(this);
        super.onCreate();
    }
}
