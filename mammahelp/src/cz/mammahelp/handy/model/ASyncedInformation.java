package cz.mammahelp.handy.model;

import java.util.Calendar;
import java.util.Date;

/**
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
public abstract class ASyncedInformation {

	public Calendar syncTime;
	public String body;
	public String title;
	public String url;
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;

	}

	public Calendar getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(Calendar syncTime) {
		this.syncTime = syncTime;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
