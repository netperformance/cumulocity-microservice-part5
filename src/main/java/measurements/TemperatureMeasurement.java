package measurements;

import java.math.BigDecimal;

import org.svenson.AbstractDynamicProperties;

import com.cumulocity.model.measurement.MeasurementValue;

public class TemperatureMeasurement extends AbstractDynamicProperties {

	private static final long serialVersionUID = 1L;

	public static final String UNIT = "C";
	
	private MeasurementValue temperatureInside = new MeasurementValue(UNIT);
	private MeasurementValue temperatureOutside = new MeasurementValue(UNIT);
	
	public MeasurementValue getTemperatureInside() {
		return temperatureInside;
	}
	public void setTemperatureInside(MeasurementValue temperatureInside) {
		this.temperatureInside = temperatureInside;
	}
	public MeasurementValue getTemperatureOutside() {
		return temperatureOutside;
	}
	public void setTemperatureOutside(MeasurementValue temperatureOutside) {
		this.temperatureOutside = temperatureOutside;
	}	
	
	// getter/setter temperature inside
	public BigDecimal getTemperatureInsideValue() {
		return temperatureInside.getValue();
	}
	public void setTemperatureInsideValue(BigDecimal temperatureValue) {
		temperatureInside.setValue(temperatureValue);
	}

	// getter/setter temperature outside
	public BigDecimal getTemperatureOutsideValue() {
		return temperatureOutside.getValue();
	}
	public void setTemperatureOutsideValue(BigDecimal temperatureValue) {
		temperatureOutside.setValue(temperatureValue);
	}
	
}
