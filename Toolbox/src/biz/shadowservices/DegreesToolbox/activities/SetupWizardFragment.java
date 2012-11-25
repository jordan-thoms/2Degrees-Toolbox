package biz.shadowservices.DegreesToolbox.activities;

import com.WazaBe.HoloEverywhere.app.AlertDialog;

import de.quist.app.errorreporter.ExceptionReporter;
import biz.shadowservices.DegreesToolbox.R;
import biz.shadowservices.DegreesToolbox.data.DataFetcher;
import biz.shadowservices.DegreesToolbox.data.DataFetcher.FetchResult;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SetupWizardFragment extends Fragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
	}
	private ProgressDialog progressDialog;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.setup_wizard_fragment, container, false);

		Button goButton = (Button) view.findViewById(R.id.setupWizardGoButton);
		goButton.setOnClickListener(goButtonListener);
		EditText password = (EditText) view.findViewById(R.id.passwordSetupWizard);
		password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
		// This gives us the masking that you see in your password fields
		password.setTransformationMethod(new PasswordTransformationMethod());
		TextView t2 = (TextView) view.findViewById(R.id.setupWizardText);
		t2.setMovementMethod(LinkMovementMethod.getInstance());

		return view;
	}

	/*    	
	 */

	private OnClickListener goButtonListener = new OnClickListener() {
		public void onClick(View v) {
			progressDialog = ProgressDialog.show(getActivity(), null , " Logging in...", true);
			progressDialog.show();

			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(v.getContext());
			Editor editor = sp.edit();
			EditText username = (EditText) getView().findViewById(R.id.usernameSetupWizard);
			EditText password = (EditText) getView().findViewById(R.id.passwordSetupWizard);
			editor.putString("username", username.getText().toString());
			editor.putString("password", password.getText().toString());
			editor.commit();
			DownloadFilesTask task = new DownloadFilesTask();
			task.execute();
		}
	};


	private class DownloadFilesTask extends AsyncTask<Void, Void, FetchResult> {
		@Override
		protected FetchResult doInBackground(Void... nothing) {
			DataFetcher dataFetcher = new DataFetcher(ExceptionReporter.register(getActivity()));
			FetchResult result =  dataFetcher.updateData(getActivity().getApplicationContext(), true);
			return result;
		}

		@Override
		protected void onPostExecute(FetchResult result) {
			if (result == FetchResult.SUCCESS) {
				getActivity().finish();
			} else {
				if(progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
				new AlertDialog.Builder(getActivity()).setMessage(result.getMessage()).setPositiveButton("OK", null).show();
			}
		}
	}
}
