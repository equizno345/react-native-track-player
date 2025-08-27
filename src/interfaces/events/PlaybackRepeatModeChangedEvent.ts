import type { RepeatMode } from '../../constants';

export interface PlaybackRepeatModeChangedEvent {
  /** The new repeat mode of the player. */
  repeatMode: RepeatMode;
}
