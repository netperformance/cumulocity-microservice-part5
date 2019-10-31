package c8y.example;

import java.math.BigDecimal;

import org.apache.commons.lang.math.RandomUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cumulocity.microservice.autoconfigure.MicroserviceApplication;
import com.cumulocity.microservice.settings.service.MicroserviceSettingsService;
import com.cumulocity.microservice.subscription.service.MicroserviceSubscriptionsService;
import com.cumulocity.model.ID;
import com.cumulocity.model.idtype.GId;
import com.cumulocity.rest.representation.identity.ExternalIDRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjects;
import com.cumulocity.rest.representation.measurement.MeasurementRepresentation;
import com.cumulocity.sdk.client.SDKException;
import com.cumulocity.sdk.client.identity.IdentityApi;
import com.cumulocity.sdk.client.inventory.InventoryApi;
import com.cumulocity.sdk.client.measurement.MeasurementApi;

import c8y.IsDevice;
import measurements.TemperatureMeasurement;

@MicroserviceApplication
@RestController
public class App{
		
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @RequestMapping("hello")
    public String greeting(@RequestParam(value = "name", defaultValue = "world") String name) {
        return "hello " + name + "!";
    }

    // You need the inventory API to handle managed objects e.g. creation. You will find this class within the C8Y java client library.
    private final InventoryApi inventoryApi;
    // you need the identity API to handle the external ID e.g. IMEI of a managed object. You will find this class within the C8Y java client library.
    private final IdentityApi identityApi;
    
    // you need the measurement API to handle measurements. You will find this class within the C8Y java client library.
    private final MeasurementApi measurementApi;
    
    // Microservice subscription
    private final MicroserviceSubscriptionsService subscriptionService;
        
    // To access the tenant options
    private final MicroserviceSettingsService microserviceSettingsService;
    
    @Autowired
    public App( InventoryApi inventoryApi, 
    			IdentityApi identityApi, 
    			MicroserviceSubscriptionsService subscriptionService,
    			MeasurementApi measurementApi,
    			MicroserviceSettingsService microserviceSettingsService) {
        this.inventoryApi = inventoryApi;
        this.identityApi = identityApi;
        this.subscriptionService = subscriptionService;
        this.measurementApi = measurementApi;
        this.microserviceSettingsService = microserviceSettingsService;
    }
    
    // Create every x sec a new measurement
    @Scheduled(initialDelay=10000, fixedDelay=5000)
    public void startThread() {
    	subscriptionService.runForEachTenant(new Runnable() {
			@Override
			public void run() {
		    	ManagedObjectRepresentation managedObjectRepresentation = resolveManagedObject();
		    	try {
		    		// createTemperatureMeasurement(managedObjectRepresentation);
		    		createMixedMeasurements(managedObjectRepresentation);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		});
    }    
        
    /*/
    1. Create a new managed object + external ID
    2. Create a new custom temperature measurement (scheduling every 1 sec.)
    3. Create a new measurement which contains 3 measurements (humidity, temperature inside and outside)
    //*/

    // 1. Create a new managed object + external ID (if not existing)  
    private ManagedObjectRepresentation resolveManagedObject() {
       	
    	try {
        	// check if managed object is existing. create a new one if the managed object is not existing
    		ExternalIDRepresentation externalIDRepresentation = identityApi.getExternalId(new ID("c8y_Serial", "Microservice-Part5_externalId"));
			return externalIDRepresentation.getManagedObject();    	    	

    	} catch(SDKException e) {
    		    		
    		// create a new managed object
			ManagedObjectRepresentation newManagedObject = new ManagedObjectRepresentation();
	    	newManagedObject.setName("Microservice-Part5");
	    	newManagedObject.setType("Microservice-Part5");
	    	newManagedObject.set(new IsDevice());	    	
	    	ManagedObjectRepresentation createdManagedObject = inventoryApi.create(newManagedObject);
	    	
	    	// create an external id and add the external id to an existing managed object
	    	ExternalIDRepresentation externalIDRepresentation = new ExternalIDRepresentation();
	    	// Definition of the external id
	    	externalIDRepresentation.setExternalId("Microservice-Part5_externalId");
	    	// Assign the external id to an existing managed object
	    	externalIDRepresentation.setManagedObject(createdManagedObject);
	    	// Definition of the serial
	    	externalIDRepresentation.setType("c8y_Serial");
	    	// Creation of the external id
	    	identityApi.create(externalIDRepresentation);
	    	
	    	return createdManagedObject;
    	}
    }
    
    
    // 2. Create a new custom measurement (CustomTemperatureMeasurement.java) 
	public void createTemperatureMeasurement(ManagedObjectRepresentation managedObjectRepresentation) {
		
		// Create a new custom temperature measurement
		CustomTemperatureMeasurement customTemperatureMeasurement = new CustomTemperatureMeasurement();
		// Set the temperature random value
		customTemperatureMeasurement.setTemperature(BigDecimal.valueOf(RandomUtils.nextInt(100)));
		
		// Create a new measurement representation
		MeasurementRepresentation measurementRepresentation = new MeasurementRepresentation();
		// Define the managed object where you would like to send the measurements
		measurementRepresentation.setSource(ManagedObjects.asManagedObject(GId.asGId(managedObjectRepresentation.getId())));
		// Set the generation time of the measurement
		measurementRepresentation.setDateTime(new DateTime());
		// Set the type of the planned measurement e.g. temperature
		measurementRepresentation.setType("c8y_CustomTemperatureMeasurement");
		// Set the temperature measurement you defined before
		measurementRepresentation.set(customTemperatureMeasurement);
		
		// Create the measurement
		measurementApi.create(measurementRepresentation);
	}
	
	// 3. Create a new measurement which contains 3 measurements (humidity, temperature inside and outside)
	public void createMixedMeasurements(ManagedObjectRepresentation managedObjectRepresentation) {
		
		measurements.HumidityMeasurement humidityMeasurement = new measurements.HumidityMeasurement();
		humidityMeasurement.setHumidityValue(BigDecimal.valueOf(RandomUtils.nextInt(100)));
		
		measurements.TemperatureMeasurement temperatureMeasurement = new TemperatureMeasurement();
		temperatureMeasurement.setTemperatureInsideValue(BigDecimal.valueOf(RandomUtils.nextInt(100)));
		temperatureMeasurement.setTemperatureOutsideValue(BigDecimal.valueOf(RandomUtils.nextInt(100)));
		
		
		// Create a new measurement representation
		MeasurementRepresentation measurementRepresentation = new MeasurementRepresentation();
		// Define the managed object where you would like to send the measurements
		measurementRepresentation.setSource(ManagedObjects.asManagedObject(GId.asGId(managedObjectRepresentation.getId())));
		// Set the generation time of the measurement
		measurementRepresentation.setDateTime(new DateTime());
		// Set the type of the planned measurement e.g. temperature
		measurementRepresentation.setType("c8y_CustomMixedMeasurement");
		
		// Set all the measurements (humidity & temperature) you defined before to the representation
		measurementRepresentation.set(humidityMeasurement);
		measurementRepresentation.set(temperatureMeasurement);
		
		// Create the measurement
		measurementApi.create(measurementRepresentation);
	}
	
    
}