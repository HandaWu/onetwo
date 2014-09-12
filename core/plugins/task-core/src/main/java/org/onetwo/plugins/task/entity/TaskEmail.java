package org.onetwo.plugins.task.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.onetwo.common.utils.GuavaUtils;
import org.onetwo.common.utils.annotation.IgnoreJson;
import org.onetwo.plugins.email.ContentType;
import org.onetwo.plugins.task.utils.TaskType;
import org.onetwo.plugins.task.utils.TaskUtils;

@Entity
@Table(name="TASK_EMAIL")
public class TaskEmail extends TaskBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6892549859686221264L;
	private String subject;
	private String content;
	private String attachmentPath;
	@Column(name="IS_HTML")
	private boolean html;
	
	private String ccAddress;
	private String toAddress;
	
	@Enumerated(EnumType.STRING)
	private ContentType contentType;
	
	public TaskEmail(){
		setType(TaskType.EMAIL.toString());
	}
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getAttachmentPath() {
		return attachmentPath;
	}

	public void setAttachmentPath(String attachmentPath) {
		this.attachmentPath = attachmentPath;
	}

	public boolean isHtml() {
		return html;
	}
	public void setHtml(boolean html) {
		this.html = html;
	}

	@IgnoreJson
	public String[] getCcAsArray() {
		return GuavaUtils.split(ccAddress, TaskUtils.ATTACHMENT_PATH_SPLITTER_CHAR);
	}

	@IgnoreJson
	public String[] getToAsArray() {
		return GuavaUtils.split(toAddress, TaskUtils.ATTACHMENT_PATH_SPLITTER_CHAR);
	}

	@IgnoreJson
	public String[] getAttachmentPathAsArray() {
		return GuavaUtils.split(getAttachmentPath(), TaskUtils.ATTACHMENT_PATH_SPLITTER);
	}


	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	public String getCcAddress() {
		return ccAddress;
	}

	public void setCcAddress(String ccAddress) {
		this.ccAddress = ccAddress;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}
	
}