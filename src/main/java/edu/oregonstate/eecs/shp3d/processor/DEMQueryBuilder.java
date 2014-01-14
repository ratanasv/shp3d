package edu.oregonstate.eecs.shp3d.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.Integer;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

final class DEMQueryBuilder {
	private String baseURL;
	private Map<QueryParameter, Object> parameters; 
	
	private enum QueryParameter {
		NUM_LATS("numlats"),
		NUM_LNGS("numlngs"),
		LAT1("lat1"),
		LAT2("lat2"),
		LNG1("lng1"),
		LNG2("lng2");
		
		private final String stringRep;
		
		QueryParameter(String stringRep){
			this.stringRep = stringRep;
		}
		
		@Override
		public String toString() {
			return this.stringRep;
		}
	}
	
	private DEMQueryBuilder(String baseURL) {
		if (baseURL.charAt(baseURL.length() - 1) != '?') {
			throw new IllegalArgumentException("baseURL doesn't end with ?");
		}
		parameters = new HashMap<QueryParameter, Object>();
		this.baseURL = baseURL;
	}
	
	static DEMQueryBuilder startBuilding(String baseURL) {
		DEMQueryBuilder newInstance = new DEMQueryBuilder(baseURL);
		return newInstance;
	}
	
	DEMQueryBuilder withNumLats(int num) {
		parameters.put(QueryParameter.NUM_LATS, new Integer(num));
		return this;
	}
	
	DEMQueryBuilder withNumLngs(int num) {
		parameters.put(QueryParameter.NUM_LNGS, new Integer(num));
		return this;
	}

	DEMQueryBuilder withLat1(float num) {
		parameters.put(QueryParameter.LAT1, new Float(num));
		return this;
	}
	
	DEMQueryBuilder withLat2(float num) {
		parameters.put(QueryParameter.LAT2, new Float(num));
		return this;
	}
	
	DEMQueryBuilder withLng1(float num) {
		parameters.put(QueryParameter.LNG1, new Float(num));
		return this;
	}
	
	DEMQueryBuilder withLng2(float num) {
		parameters.put(QueryParameter.LNG2, new Float(num));
		return this;
	}
	
	String build() {
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (QueryParameter param : QueryParameter.values()) {
			if (parameters.get(param) == null ) {
				throw new IllegalStateException("missing DEM parameter " + param.toString());
			}
			params.add(new BasicNameValuePair(param.toString(), parameters.get(param).toString()));
		}
		return this.baseURL + URLEncodedUtils.format(params, HTTP.UTF_8);
	}
}
