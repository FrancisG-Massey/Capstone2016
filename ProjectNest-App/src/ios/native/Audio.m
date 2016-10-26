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
#import <UIKit/UIKit.h>
#include "jni.h"
#import <AVFoundation/AVFoundation.h>

AVAudioPlayer* player;

JNIEXPORT void JNICALL Java_org_nestnz_app_services_ios_IOSAudioService_audioPlay
(JNIEnv *env, jclass jClass, jstring jDir, double volume) {
    const jchar *charsDir = (*env)->GetStringChars(env, jDir, NULL);
    NSString *dir = [NSString stringWithCharacters:(UniChar *)charsDir length:(*env)->GetStringLength(env, jDir)];
    (*env)->ReleaseStringChars(env, jDir, charsDir);
    
    NSString* fileName = [dir stringByDeletingPathExtension];
    NSString* extension = [dir pathExtension];

    NSURL* url = [[NSBundle mainBundle] URLForResource:[NSString stringWithFormat:@"%@",fileName] withExtension:[NSString stringWithFormat:@"%@",extension]];
    NSError* error = nil;

    if(player) {
        [player stop];
        player = nil;
    }

    player = [[AVAudioPlayer alloc] initWithContentsOfURL:url error:&error];
    if(!player) {
        NSLog(@"Error creating player: %@", error);
        return;
    }
    player.delegate = self;
    [player setVolume: volume];
    [player prepareToPlay];
    [player play];

}

JNIEXPORT void JNICALL Java_org_nestnz_app_services_ios_IOSAudioService_audioStop
(JNIEnv *env, jclass jClass) {
    if(!player)
    {
        return;
    }
    [player stop];
    player = nil;
}

JNIEXPORT void JNICALL Java_org_nestnz_app_services_ios_IOSAudioService_audioPause
(JNIEnv *env, jclass jClass) {
    if(!player)
    {
        return;
    }
    [player pause];
}

JNIEXPORT void JNICALL Java_org_nestnz_app_services_ios_IOSAudioService_audioResume
(JNIEnv *env, jclass jClass){
    if(!player)
    {
        return;
    }
    [player play];
}

- (void)audioPlayerDidFinishPlaying:(AVAudioPlayer *)player successfully:(BOOL)flag
{
    NSLog(@"%s successfully=%@", __PRETTY_FUNCTION__, flag ? @"YES"  : @"NO");
}

- (void)audioPlayerDecodeErrorDidOccur:(AVAudioPlayer *)player error:(NSError *)error
{
    NSLog(@"%s error=%@", __PRETTY_FUNCTION__, error);
}

@end
