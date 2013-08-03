/*
    This file is part of Project MAXS.

    MAXS and its modules is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MAXS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MAXS.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.projectmaxs.main.receivers;

import org.projectmaxs.main.MAXSService;
import org.projectmaxs.main.Settings;
import org.projectmaxs.main.util.Constants;
import org.projectmaxs.shared.util.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkConnectivityReceiver extends BroadcastReceiver {

	private static String sLastActiveNetworkType = null;
	private static Log sLog = Log.getLog();

	@Override
	public void onReceive(Context context, Intent intent) {
		Settings settings = Settings.getInstance(context);

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (settings.isDebugLogEnabled()) {
			for (NetworkInfo networkInfo : cm.getAllNetworkInfo())
				log(networkInfo);
		}

		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
		if (activeNetworkInfo != null) {
			sLog.d("ActiveNetworkInfo follows:");
			log(activeNetworkInfo);
		}

		boolean connected;
		boolean networkTypeChanged;

		if (MAXSService.isRunning()) {
			if (activeNetworkInfo != null) {
				// we have an active data connection
				String networkTypeName = activeNetworkInfo.getTypeName();
				connected = true;
				networkTypeChanged = false;
				if (!networkTypeName.equals(sLastActiveNetworkType)) {
					sLastActiveNetworkType = networkTypeName;
					networkTypeChanged = true;
				}
			}
			else {
				// we have *no* active data connection
				if (sLastActiveNetworkType != null) {
					networkTypeChanged = true;
				}
				else {
					networkTypeChanged = false;
				}
				connected = false;
				sLastActiveNetworkType = null;
			}
			Intent i = new Intent(Constants.ACTION_NETWORK_STATUS_CHANGED);
			i.putExtra(Constants.EXTRA_NETWORK_TYPE_CHANGED, networkTypeChanged);
			i.putExtra(Constants.EXTRA_NETWORK_CONNECTED, connected);
			context.startService(i);
		}
	}

	private static void log(NetworkInfo networkInfo) {
		// @formatter:off
		sLog.d("networkName=" + networkInfo.getTypeName() 
				+ " available=" + networkInfo.isAvailable()
				+ ", connected=" + networkInfo.isConnected()
				+ ", connectedOrConnecting=" + networkInfo.isConnectedOrConnecting()
				+ ", failover=" + networkInfo.isFailover()
				+ ", roaming=" + networkInfo.isRoaming());
		// @formatter:on
	}
}