package measurements;

import java.math.BigDecimal;

import org.svenson.AbstractDynamicProperties;

import com.cumulocity.model.measurement.MeasurementValue;

public class HumidityMeasurement extends AbstractDynamicProperties {

	private static final long serialVersionUID = 1L;
	
	public static final String UNIT = "H";
	
	private MeasurementValue humidity = new MeasurementValue(UNIT);
	
	public MeasurementValue getHumidity() {
		return humidity;
	}

	public void setHumidity(MeasurementValue humidity) {
		this.humidity = humidity;
	}

	// Get the temperature (value)
	public BigDecimal getHumidityValue() {
		return humidity.getValue();
	}
	
	// Set the temperature (value)
	public void setHumidityValue(BigDecimal humidityValue) {
		humidity.setValue(humidityValue);
	}
}
