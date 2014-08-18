package cz.mammahelp.handy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import net.suteren.android.jidelak.model.Availability;
import net.suteren.android.jidelak.model.Meal;
import net.suteren.android.jidelak.model.Restaurant;
import net.suteren.android.jidelak.model.Source;
import android.content.Context;

public class JidelakException extends Exception {

	private int resource;
	private String[] args;

	private ErrorType errorType;
	private boolean handled = false;

	public JidelakException(int messageToUser, Throwable e) {
		super(e);
		setResource(messageToUser);
	}

	public JidelakException(int messageToUser) {
		super();
		setResource(messageToUser);
	}

	public JidelakException(int messageToUser, String... strings) {
		this(messageToUser);
		args = strings;
	}

	public JidelakException(int malformedUrl, Exception e, String... strings) {
		this(malformedUrl, e);
		args = strings;
	}

	public void setResource(int messageToUser) {
		resource = messageToUser;
	}

	public int getResource() {
		return resource;
	}

	public JidelakException setArgs(String[] args) {
		this.args = args;
		return this;
	}

	public String[] getArgs() {

		ArrayList<String> args = new ArrayList<String>();

		args.add(getRestaurant() == null ? ""
				: getRestaurant().getName() == null ? String.format(
						"restaurant id: %d", getRestaurant().getId())
						: getRestaurant().getName());
		args.add(getSource() == null || getSource().getUrl() == null ? ""
				: getSource().getUrl().toString());
		args.add(getMeal() == null ? "" : getMeal().getTitle());
		args.add(getAvailability() == null ? "" : getAvailability().toString());

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

	public Meal getMeal() {
		return meal;
	}

	public JidelakException setMeal(Meal meal) {
		this.meal = meal;
		return this;
	}

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public JidelakException setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
		return this;
	}

	public Source getSource() {
		return source;
	}

	public JidelakException setSource(Source source) {
		this.source = source;
		return this;
	}

	public Availability getAvailability() {
		return availability;
	}

	public JidelakException setAvailability(Availability availability) {
		this.availability = availability;
		return this;
	}

	public boolean isHandled() {
		return handled;
	}

	public JidelakException setHandled(boolean handled) {
		this.handled = handled;
		return this;
	}

	public ErrorType getErrorType() {
		return errorType;
	}

	public JidelakException setErrorType(ErrorType errorType) {
		this.errorType = errorType;
		return this;
	}
}
