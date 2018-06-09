import java.util.Scanner

fun main(args: Array<String>) {
    val gameStaus : GAME_STATUS = GAME_STATUS()
    val board : BOARD_DISP = BOARD_DISP(gameStaus)
    val masuSel : MASU_SEL = MASU_SEL()

    var playing : Boolean = true 
    do {
        when( gameStaus.playPhase) {
            GAME_STATUS.GAME_PHASE.INITIAL -> {
                board.boardDisp()   // 初期状態表示
                gameStaus.gameMode = masuSel.modeInput()
                gameStaus.playPhase = GAME_STATUS.GAME_PHASE.RESET
            }
            GAME_STATUS.GAME_PHASE.RESET -> {
                gameStaus.gameReset()
                gameStaus.playPhase = gameStaus.nextPhase()
            }
            GAME_STATUS.GAME_PHASE.PLAY1 -> {
                gameStaus.checkReayMasu()   // 
                gameStaus.getKomaCount()    // 
                board.boardDisp()           // 
                val infoMmsg : String = if (gameStaus.gameMode==GAME_STATUS.GAME_MODE.MAN_TO_MAN) {
                        "PLAYER-1"
                    } else {
                        "YOU"
                    }
                gameStaus.masuSelect(masuSel.playerInput(infoMmsg))
            }
            GAME_STATUS.GAME_PHASE.PLAY2 -> {
                gameStaus.checkReayMasu()   // 
                gameStaus.getKomaCount()    // 
                board.boardDisp()           // 
                gameStaus.masuSelect(masuSel.playerInput("PLAYER-2"))
            }
            GAME_STATUS.GAME_PHASE.AUTO1-> {
                gameStaus.checkReayMasu()
                gameStaus.getKomaCount()    // 
                board.boardDisp()           // 
                gameStaus.masuSelect(gameStaus.autoActionSelect())
            }
            GAME_STATUS.GAME_PHASE.AUTO2-> {
                gameStaus.checkReayMasu()
                gameStaus.getKomaCount()    // 
                board.boardDisp()           // 
                gameStaus.masuSelect(gameStaus.autoActionSelect())
            }
            GAME_STATUS.GAME_PHASE.WINNER_FIX -> {
                gameStaus.masuRefresh()
                gameStaus.gemeWinner = 
                    if (gameStaus.gemeWinner==GAME_STATUS.WINNER.NON) {
                        if (gameStaus.playerKomaCount[0]>gameStaus.playerKomaCount[1]) {
                            GAME_STATUS.WINNER.PLAYER1
                        } else {
                            GAME_STATUS.WINNER.PLAYER1
                        }
                    } else {
                        gameStaus.gemeWinner // そのまま
                    }
                gameStaus.getKomaCount()
                board.boardDisp( )
                println("WINNER : " + gameStaus.gemeWinner.toString() )
                gameStaus.playPhase = GAME_STATUS.GAME_PHASE.GAME_OVER
            }
            else -> { 
                playing = false
            }
        }
    } while ( playing )

}

class MASU_SEL() {
    val input = Scanner( System.`in` )

    fun modeInput( ) : GAME_STATUS.GAME_MODE {
        print(" Player Select : 0=対人, 1=コンピュータ > ")
        return when( input.nextInt()) {
            1 ->    GAME_STATUS.GAME_MODE.MAN_TO_AUTO
            2 ->    GAME_STATUS.GAME_MODE.AUTO_TO_AUTO
            else -> GAME_STATUS.GAME_MODE.MAN_TO_MAN
        }
    }

    // マスの座標入力（選択)
    fun playerInput(player : String ) :SELECT_MASU {
        val xy : SELECT_MASU = SELECT_MASU()
        var inWord : String 

        do {
            print(player + " > ")
            inWord = input.next()
            if ( inWord.length >= 2 ) {
                if ( inWord == "end" ) {    // 降参
                    xy.InSw = SELECT_MASU.IN_STATUS.SURRENDER
                } else if ( inWord == "pass" ) {
                    // 今はパスに回数の制限なし
                    xy.InSw = SELECT_MASU.IN_STATUS.PASS
                } else {
                    xy.x = if ((inWord[0].toInt() >= 'a'.toInt())&&(inWord[0].toInt() <= 'h'.toInt())) {
                            inWord[0].toInt() - 'a'.toInt()  
                        } else if ((inWord[0].toInt() >= 'A'.toInt())&&(inWord[0].toInt() <= 'H'.toInt())) {
                            inWord[0].toInt() - 'A'.toInt()  
                        } else {
                            9
                        }
                    xy.y = if ((inWord[1].toInt()>='1'.toInt()) && (inWord[1].toInt()<='8'.toInt())) {
                            (inWord[1].toString()).toInt() - 1
                        } else {
                            9
                        }
                     xy.InSw = if (xy.x!=9 && xy.y!=9) {
                            SELECT_MASU.IN_STATUS.MASU_SELECT   // マス選択
                        } else {
                            SELECT_MASU.IN_STATUS.NON           // 無効な入力
                        }
                }
            }
        } while (xy.InSw == SELECT_MASU.IN_STATUS.NON )
        return xy
    }
} 

// ボード 表示クラス
class BOARD_DISP( val boardStatus : GAME_STATUS ) {
    // ボードの表示
    fun boardDisp() {
   		println("   a b c d e f g h")
        println("   -----------------")
        for( y : Int in 0..7) {
            print((y+1).toString() + "|")
            for( x : Int in 0..7) {
                var z : String = when(boardStatus.banStatus[x][y]) {
                    GAME_STATUS.MASU_STATUS.READY -> " 0"
                    GAME_STATUS.MASU_STATUS.WHITE -> " 1"
                    GAME_STATUS.MASU_STATUS.BLACK -> " 2"
                    else -> " ."
                }
                print(z)
            }
            println("")
        }
        println("PLAYER-1 = "+(boardStatus.playerKomaCount[0]).toString()+" , PLAYER-2 = "+(boardStatus.playerKomaCount[1]).toString() )
    }

}


// マスの情報データクラス
class SELECT_MASU( var x: Int=0 , var y:Int=0 , var InSw:IN_STATUS=IN_STATUS.NON ) {
    // 入力にも使用しているので、下記enumつけてみた
    enum class IN_STATUS(var IntVal :Int ) {
        NON(0),
        MASU_SELECT(1),
        SURRENDER(2),
        PASS(3),
    }
}

class GAME_STATUS {
    // 対戦モード (PLAYER2)
    enum class GAME_MODE(val IntVal : Int ) {
        MAN_TO_MAN(0),
        MAN_TO_AUTO(1),
        AUTO_TO_AUTO(2)
    }
    var gameMode : GAME_MODE = GAME_MODE.MAN_TO_MAN

    // フェーズ
    enum class GAME_PHASE(val intVal : Int ) {
        INITIAL(0),
        RESET(1),
        PLAY1(2),
        PLAY2(3),
        AUTO1(4),
        AUTO2(5),
        WINNER_FIX(6),
        GAME_OVER(7),
    }
    var playPhase : GAME_PHASE = GAME_PHASE.INITIAL

    // マスのステータス
    enum class  MASU_STATUS( val indxVal : Int) {
        NORMAL(0),         // 何でもなし
        READY(1),          // 置いていい
        BLACK(2),          // 黒
        WHITE(3),          // 白
        OUT_RANGE(4),
    }
    // マス８×８　NORMAL で 埋めている
    var banStatus  =  Array(8, {Array(8, {MASU_STATUS.NORMAL })})
    // 駒の数
    var playerKomaCount = arrayOf<Int>( 0 , 0 )

    // 勝者
    enum class WINNER {
        NON,
        PLAYER1,
        PLAYER2,
    }
    var  gemeWinner : WINNER = WINNER.NON

    // 方向
    enum class VECTOR(val intVal : Int ) {
        UP(0),
        RIGHT_UP(1),
        RIGHT(2),
        RIGHT_DOWN(3),
        DOWN(4),
        LEFT_DOWN(5),
        LEFT(6),
        LEFT_UP(7),
    }
    // 方向による 座標の補正データ x+VX[VECTOR.UP]*3 で [xの上方向、距離３]になる
    val VX = arrayOf<Int>( 0,  1, 1, 1, 0, -1, -1, -1 )
    val VY = arrayOf<Int>(-1, -1, 0, 1, 1,  1,  0, -1 )

    // プレイヤーと駒の関係
    enum class SITUATION( val intVal : Int ) {
        ALLY(0),
        ENEMY(1),
    }
    val player1Koma : MASU_STATUS = MASU_STATUS.WHITE
    val player2Koma : MASU_STATUS = MASU_STATUS.BLACK
    fun getKomaStatus( newPhase : GAME_PHASE, getMode : SITUATION ) : MASU_STATUS {
        return if ( newPhase == GAME_PHASE.PLAY1) {
                if ( getMode == SITUATION.ALLY) {
                    player1Koma
                } else {
                    player2Koma
                }
            } else {
                if ( getMode == SITUATION.ALLY) {
                    player2Koma
                } else {
                    player1Koma
                }
            }
    }

    val auto1 : AUTO1 = AUTO1( banStatus )
    val auto2 : AUTO1 = AUTO1( banStatus )

    // コマ数　数える
    fun getKomaCount( ) {
        playerKomaCount[0] = 0
        playerKomaCount[1] = 0
        for( x:Int in 0..7) {
            for(y:Int in 0..7) {
                when(banStatus[x][y]) {
                    MASU_STATUS.WHITE -> playerKomaCount[0]++
                    MASU_STATUS.BLACK -> playerKomaCount[1]++
                    else -> {}
                }
            }
        }
    }

    // ゲームのリセット
    fun gameReset(  ) {
        for (x: Int in 0..7) {
            for (y: Int in 0..7) {
                banStatus[x][y] = MASU_STATUS.NORMAL
            }
        }
        banStatus[3][3] = player1Koma
        banStatus[3][4] = player2Koma
        banStatus[4][3] = player2Koma
        banStatus[4][4] = player1Koma
        playerKomaCount[0] = 2
        playerKomaCount[1] = 2
    }

    // 入力データに応じた処理
    fun masuSelect( selMasu : SELECT_MASU ) {
        when( selMasu.InSw ) {
            SELECT_MASU.IN_STATUS.SURRENDER -> {
                // 降参 
                playPhase = GAME_PHASE.WINNER_FIX
                // 降参した場合は相手の勝ち
                gemeWinner = if (playPhase==GAME_PHASE.PLAY2) {
                    WINNER.PLAYER1
                } else {
                    WINNER.PLAYER2
                }
            }
            SELECT_MASU.IN_STATUS.PASS -> {
                // パス 回数に今は上限はなし
                playPhase = nextPhase()
            }
            SELECT_MASU.IN_STATUS.MASU_SELECT -> {
                if (MASU_STATUS.READY == banStatus[selMasu.x][selMasu.y]) {
                    banStatus[selMasu.x][selMasu.y] = getKomaStatus(playPhase,SITUATION.ALLY)
                    ReversKoma(selMasu)
                    playPhase = nextPhase()
                }
            }
            else -> { }
        }
    }
    // 置けるマスの確認
    fun checkReayMasu() {
        masuRefresh()
        // 総当りだけど・・
        for( x:Int in 0..7) {
            for(y:Int in 0..7) {
                if (banStatus[x][y] == MASU_STATUS.NORMAL ) {
                   val xy : SELECT_MASU = SELECT_MASU(x,y,SELECT_MASU.IN_STATUS.NON)

                    for(v:Int in VECTOR.UP.intVal..VECTOR.LEFT_UP.intVal ) {
                        if ( true == checkRevers(xy, playPhase, v)) {
                            banStatus[x][y] = MASU_STATUS.READY
                            break
                        }
                    }
                }
            }
        }
    }

    // 反転
    fun ReversKoma(xy:SELECT_MASU) {
        for(v:Int in VECTOR.UP.intVal..VECTOR.LEFT_UP.intVal ) {
            if ( true == checkRevers(xy, playPhase, v)) {
                for ( i:Int in 1..7 ) {
                    val x = xy.x + (VX[v]*i)
                    val y = xy.y + (VY[v]*i)
                    if (x>0 && x<8 && y>0 && y<8) {
                        if (banStatus[x][y]==getKomaStatus(playPhase,SITUATION.ENEMY)) {
                            banStatus[x][y] =getKomaStatus(playPhase,SITUATION.ALLY)
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }
            }
        }
    }

    //　マシンの行動選択
    fun autoActionSelect() : SELECT_MASU {
        val r = if ( playPhase==GAME_PHASE.PLAY1 ) {
                auto1.actSelect()
            } else {
                auto2.actSelect()
            }
        return r
    }

    // フェーズを進める
    fun nextPhase() : GAME_PHASE { 
        masuRefresh()
        return  if ( checkEndGame() == false ) {
                    GAME_PHASE.WINNER_FIX
                } else if (playPhase==GAME_PHASE.PLAY1) {
                    if (gameMode==GAME_MODE.MAN_TO_MAN) {
                        GAME_PHASE.PLAY2
                    } else {
                        GAME_PHASE.AUTO2
                    }
                } else {
                    if (gameMode==GAME_MODE.AUTO_TO_AUTO) {
                        GAME_PHASE.AUTO1
                    } else {
                        GAME_PHASE.PLAY1 
                    }
                } 
    }


    // 前のREADYをクリア
    fun masuRefresh() {
        for(x:Int in 0..7) {
            for(y:Int in 0..7) {
                if (banStatus[x][y] ==MASU_STATUS.READY) {
                    banStatus[x][y] = MASU_STATUS.NORMAL
                }
            }
        }
    }

    // 基準座標の指定方向に、反転可能な駒があるか？
    fun checkRevers(xy:SELECT_MASU, player:GAME_PHASE, v:Int) : Boolean {
        var cnt:Int = 0
        for ( i:Int in 1..7 ) {
            // 連続して「敵駒」を探す
            if (getKomaStatus(player,SITUATION.ENEMY)!=( getKoma(xy,v,i))) {
                cnt = i
                break
            }
        }
        return if ( cnt > 1 ) {
            if (getKomaStatus(player,SITUATION.ALLY)==getKoma(xy,v,cnt)) {
                // 間が「敵駒」で、端が「自駒」
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    // 基準座標、方向、距離を指定し、そのマスの駒を得る
    fun getKoma( xy:SELECT_MASU, v:Int, distance:Int ) : MASU_STATUS {
        val x = xy.x + (VX[v]*distance)
        val y = xy.y + (VY[v]*distance)
        return if (x>0 && x<8 && y>0 && y<8) {
            banStatus[x][y]
        } else {
            MASU_STATUS.OUT_RANGE
        }
    }

    // 両プレイヤーに置ける場所がない
    fun checkEndGame() : Boolean {
        for( x:Int in 0..7) {
            for(y:Int in 0..7) {
                if (banStatus[x][y] == MASU_STATUS.NORMAL ) {
                   val xy : SELECT_MASU = SELECT_MASU(x,y,SELECT_MASU.IN_STATUS.NON)

                    for(v:Int in VECTOR.UP.intVal..VECTOR.LEFT_UP.intVal ) {
                        if ( true == checkRevers(xy, playPhase, v)) {
                          return true
                        }
                    }
                }
            }
        }
        val nextPlayer =  if (playPhase==GAME_PHASE.PLAY1) { GAME_PHASE.PLAY2} else { GAME_PHASE.PLAY1 } 
        for( x:Int in 0..7) {
            for(y:Int in 0..7) {
                if (banStatus[x][y] == MASU_STATUS.NORMAL ) {
                   val xy : SELECT_MASU = SELECT_MASU(x,y,SELECT_MASU.IN_STATUS.NON)

                    for(v:Int in VECTOR.UP.intVal..VECTOR.LEFT_UP.intVal ) {
                        if ( true == checkRevers(xy, nextPlayer, v)) {
                          return true
                        }
                    }
                }
            }
        }
        return false
    }
}

// マスの重みで判断する思考パターン
class AUTO1( val ban : Array<Array<GAME_STATUS.MASU_STATUS>> )  {

    val weight = arrayOf<Array<Int>>( 
          arrayOf<Int>(  30,-12,  0, -1, -1,  0,-12, 30 ),
          arrayOf<Int>( -12,-15, -3, -3, -3, -3,-15,-12 ),
          arrayOf<Int>(   0, -3,  0, -1, -1,  0, -3,  0 ),
          arrayOf<Int>(  -1, -3, -1, -1, -1, -1, -3, -1 ),
          arrayOf<Int>(  -1, -3, -1, -1, -1, -1, -3, -1 ),
          arrayOf<Int>(   0, -3,  0, -1, -1,  0, -3,  0 ),
          arrayOf<Int>( -12,-15, -3, -3, -3, -3,-15,-12 ),
          arrayOf<Int>(  30,-12,  0, -1, -1,  0,-12, 30 )
        )

    fun actSelect() : SELECT_MASU {
        var maxW = -100
        var slectAct : SELECT_MASU = SELECT_MASU(0,0,SELECT_MASU.IN_STATUS.PASS)
        for( x:Int in 0..7) {
            for(y:Int in 0..7) {
                if ( ban[x][y] == GAME_STATUS.MASU_STATUS.READY ) {
                    if ( weight[x][y] > maxW ) {
                        maxW = weight[x][y]
                        slectAct.x = x
                        slectAct.y = y
                        slectAct.InSw = SELECT_MASU.IN_STATUS.MASU_SELECT
                    }
                }
            }
        }
        return slectAct
    }
}
