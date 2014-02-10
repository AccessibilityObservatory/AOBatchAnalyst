package edu.umn.ao.ao_batch_analyst;

import java.util.TimeZone;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.opentripplanner.util.DateUtils;

import lombok.Setter;
import lombok.Getter;

import javax.annotation.PostConstruct;

public class DepartureTimeListGenerator {
	
	@Setter private String firstDate;
	@Setter private String firstTime;
	@Setter private String lastDate;
	@Setter private String lastTime;
	@Setter @Getter private TimeZone timeZone = TimeZone.getDefault();
	@Setter private int incrementMinutes = 1;
	
	private Date firstDepTime = new Date();
	private Date lastDepTime = new Date();
	
	private List<Date> depTimes = null;
	
	@PostConstruct
	public void initializeComponent() {
		//TODO: handle various null combinations
		firstDepTime = DateUtils.toDate(firstDate, firstTime, timeZone);
		lastDepTime = DateUtils.toDate(lastDate, lastTime, timeZone);
	}
	
	public List<Date> getDepartureTimes() {
		if (depTimes != null) {
			return depTimes;
		} else {
			depTimes = new ArrayList<Date>();
			for (long t = firstDepTime.getTime(); t <= lastDepTime.getTime(); t += incrementMinutes * 60000) {
				depTimes.add(new Date(t));
			}
		return depTimes;
		}
	}
}
