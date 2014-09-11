package cz.mammahelp.handy.ui;

import android.app.Activity;
import android.app.Fragment;
import cz.mammahelp.handy.INamed;
import cz.mammahelp.handy.MammaHelpDbHelper;

public abstract class ANamedFragment extends Fragment implements INamed {

	private String name;
	private MammaHelpDbHelper dbHelper;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof AbstractMammaHelpActivity) {
			AbstractMammaHelpActivity ama = (AbstractMammaHelpActivity) activity;
			setDbHelper(ama.getDbHelper());

		}
	}

	private void setDbHelper(MammaHelpDbHelper dbHelper) {
		this.dbHelper = dbHelper;

	}

	public MammaHelpDbHelper getDbHelper() {
		return dbHelper;
	}
}
