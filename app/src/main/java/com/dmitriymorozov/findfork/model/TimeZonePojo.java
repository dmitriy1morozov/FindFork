package com.dmitriymorozov.findfork.model;

import com.google.gson.annotations.SerializedName;
import javax.annotation.Generated;


public class TimeZonePojo {
		@Generated("com.robohorse.robopojogenerator")

		@SerializedName("timeZoneName") private String timeZoneName;

		@SerializedName("rawOffset") private int rawOffset;

		@SerializedName("timeZoneId") private String timeZoneId;

		@SerializedName("dstOffset") private int dstOffset;

		@SerializedName("status") private String status;

		public void setTimeZoneName(String timeZoneName) {
				this.timeZoneName = timeZoneName;
		}

		public String getTimeZoneName() {
				return timeZoneName;
		}

		public void setRawOffset(int rawOffset) {
				this.rawOffset = rawOffset;
		}

		public int getRawOffset() {
				return rawOffset;
		}

		public void setTimeZoneId(String timeZoneId) {
				this.timeZoneId = timeZoneId;
		}

		public String getTimeZoneId() {
				return timeZoneId;
		}

		public void setDstOffset(int dstOffset) {
				this.dstOffset = dstOffset;
		}

		public int getDstOffset() {
				return dstOffset;
		}

		public void setStatus(String status) {
				this.status = status;
		}

		public String getStatus() {
				return status;
		}

		@Override public String toString() {
				return "TimeZonePojo{"
						+ "timeZoneName = '"
						+ timeZoneName
						+ '\''
						+ ",rawOffset = '"
						+ rawOffset
						+ '\''
						+ ",timeZoneId = '"
						+ timeZoneId
						+ '\''
						+ ",dstOffset = '"
						+ dstOffset
						+ '\''
						+ ",status = '"
						+ status
						+ '\''
						+ "}";
		}
}
