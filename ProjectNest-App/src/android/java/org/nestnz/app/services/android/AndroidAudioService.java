/*******************************************************************************
 * Copyright (C) 2016, Nest NZ
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.nestnz.app.services.android;

import java.io.IOException;

import org.nestnz.app.services.AudioService;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import javafxports.android.FXActivity;

/**
 * @see http://stackoverflow.com/questions/38419634/javafxports-how-to-call-android-native-media-player/38421099#38421099
 */
public class AndroidAudioService implements AudioService {
	
	private MediaPlayer mp;
    private int currentPosition;

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.AudioService#play(java.lang.String)
	 */
	@Override
	public void play(String audioName, double volume) {
		currentPosition = 0;
        try {
            if (mp != null) {
                stop();
            }
            mp = new MediaPlayer();
            AssetFileDescriptor afd = FXActivity.getInstance().getAssets().openFd(audioName);

            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.setAudioStreamType(AudioManager.STREAM_RING);
            mp.setOnCompletionListener(mp -> stop());
            mp.setVolume((float) volume, (float) volume);
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            System.out.println("Error playing audio resource " + e);
        }
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.AudioService#stop()
	 */
	@Override
	public void stop() {
		if (mp != null) {
            if (mp.isPlaying()) {
                mp.stop();
            }
            mp.release();
            mp = null;
        }
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.AudioService#pause()
	 */
	@Override
	public void pause() {
		if (mp != null) {
            mp.pause();
            currentPosition = mp.getCurrentPosition();
        }
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.AudioService#resume()
	 */
	@Override
	public void resume() {
		if (mp != null) {
            mp.start();
            mp.seekTo(currentPosition);
        }
	}

}
