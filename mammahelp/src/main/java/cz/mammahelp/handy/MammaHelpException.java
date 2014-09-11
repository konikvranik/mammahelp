package cz.mammahelp.handy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;

public class MammaHelpException extends Exception {

	private int resource;
	private String[] args;

	private ErrorType errorType;
	private boolean handled = false;

	public MammaHelpException(int messageToUser, Throwable e) {
		super(e);
		setResource(messageToUser);
	}

	public MammaHelpException(int messageToUser) {
		super();
		setResource(messageToUser);
	}

	public MammaHelpException(int messageToUser, String... strings) {
		this(messageToUser);
		args = strings;
	}

	public MammaHelpException(int malformedUrl, Exception e, String... strings) {
		this(malformedUrl, e);
		args = strings;
	}

	public void setResource(int messageToUser) {
		resource = messageToUser;
	}

	public int getResource() {
		return resource;
	}

	public MammaHelpException setArgs(String[] args) {
		this.args = args;
		return this;
	}

	public String[] getArgs() {

		ArrayList<String> args = new ArrayList<String>();

//		args.add(getRestaurant() == null ? ""
//				: getRestaurant().getName() == null ? String.format(
//						"restaurant id: %d", getRestaurant().getId())
//						: getRestaurant().getName());

		if (this.args != null)
			args.addAll(Arrays.asList(this.args));

		return args.toArray(new String[0]);
	}

	private static final long serialVersionUID = -5345112897072655374L;

	private void readObject(ObjectInputStream aInputStream)
			throws ClassNotFoundException, IOException {
		aInputStream.defaultReadObject();
	}

	private void writeObject(ObjectOutputStream aOutputStream)
			throws IOException {
		// perform the default serialization for all non-transient, non-static
		// fields
		aOutputStream.defaultWriteObject();
		// aOutputStream.writeObject(resource);
		// aOutputStream.writeObject(args);
	}

	public String toString(Context ctx) {
		return ctx.getResources()
				.getString(getResource(), (Object[]) getArgs());
	}

	

	public boolean isHandled() {
		return handled;
	}

	public MammaHelpException setHandled(boolean handled) {
		this.handled = handled;
		return this;
	}

	public ErrorType getErrorType() {
		return errorType;
	}

	public MammaHelpException setErrorType(ErrorType errorType) {
		this.errorType = errorType;
		return this;
	}
}
