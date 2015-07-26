package com.louis.bluetoothcar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	public static final String Tag="SettingsActivity";
	private static String regEx ="[0-9a-fA-F]{2} [0-9a-fA-F]{2} [0-9a-fA-F]{2} [0-9a-fA-F]{2}";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		//返回模式
		ActionBar mActionBar=getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setNavigationMode(ActionBar.DISPLAY_HOME_AS_UP);
		FragmentTransaction ft=getFragmentManager().beginTransaction();
		PreferenceFragement settings=new PreferenceFragement();
		ft.add(R.id.frame_settings, settings);
//		ft.addToBackStack(null);
		ft.commit();
		
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public static class PreferenceFragement extends PreferenceFragment{
		Boolean firstFlag=true;
        @Override  
        public void onCreate(Bundle savedInstanceState) {  
            // TODO Auto-generated method stub  
            super.onCreate(savedInstanceState);  
            addPreferencesFromResource(R.xml.preference);
            initPreferenceCategory("pref_key_allow_typedefine_commond");
        }

        public void initPreferenceCategory(String key){
//        	Log.v(Tag, "initPreferenceCategory");
        	CheckBoxPreference allow_typedefine = (CheckBoxPreference) findPreference(key);
			PreferenceCategory faker=(PreferenceCategory) findPreference("pref_key_setting_commond");
//			System.out.println("checked:" + allow_typedefine.isChecked());
			
			if(allow_typedefine.isChecked()){
						
				faker.setEnabled(true); 
			}else{
				
				faker.setEnabled(false);

			}
			
        }
        
		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
				Preference preference) {
			// TODO Auto-generated method stub
//			Log.e(Tag, "onPreferenceTreeClick");
			if("pref_key_allow_typedefine_commond".equals(preference.getKey()))
				initPreferenceCategory(preference.getKey());
			preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(Preference preference,
						Object value) {
					// TODO Auto-generated method stub
//					Log.i(Tag, "onPreferenceChange");
					if(!"pref_key_allow_typedefine_commond".equals(preference.getKey())){
						String valueStr=(String) value;
						Pattern pattern=Pattern.compile(regEx);
						Matcher m=pattern.matcher(valueStr);
						if(m.matches()){
							preference.setSummary(valueStr);
							Toast.makeText(getActivity(), "设置成功！", Toast.LENGTH_LONG).show();
							return true;
						}
						Toast.makeText(getActivity(), "格式不正确，请重新输入", Toast.LENGTH_LONG).show();
						return false;
					}
					return true;
				}
				
			});
			return super.onPreferenceTreeClick(preferenceScreen, preference);
		}
     
    }  
	

}
