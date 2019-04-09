package gamelogic;

/**
 * spike one is the source spike and spike 2 is the destination spike.
 * purely for the bot play
 */
class SpikePair {

    SpikePair(Spike pickMyPiece_,Spike dropPiecesOnMe_) {
        pickMyPiece = pickMyPiece_;
        dropPiecesOnMe = dropPiecesOnMe_;
    }
    Spike pickMyPiece;
    Spike dropPiecesOnMe;
}
