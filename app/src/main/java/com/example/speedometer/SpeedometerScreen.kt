package com.example.speedometer

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun SpeedometerScreen(){

    val coroutineScope = rememberCoroutineScope()
    val animation = remember { Animatable(0f) }
    val maxSpeed = remember { mutableStateOf(0f) }

    maxSpeed.value = max(maxSpeed.value, animation.value * 100f)

    SpeedometerScreen(state = animation.toUiState(maxSpeed.value)){
        coroutineScope.launch {
            maxSpeed.value = 0f
            startAnimation(animation)
        }
    }
}

suspend fun startAnimation(animation: Animatable<Float, AnimationVector1D>){
    animation.animateTo(0.84f, keyframes {
        durationMillis = 900
        0f at 0 with CubicBezierEasing(0f,1.5f,0.8f,1f)
        0.72f at 1000 with CubicBezierEasing(0.2f,-1.5f,0f,1f)
        0.76f at 2000 with CubicBezierEasing(0.2f,-2f,0f,1f)
        0.78f at 3000 with CubicBezierEasing(0.2f,-1.5f,0f,1f)
        0.82f at 4000 with CubicBezierEasing(0.2f,-2f,0f,1f)
        0.85f at 5000 with CubicBezierEasing(0.2f,-2f,0f,1f)
        0.89f at 6000 with CubicBezierEasing(0.2f,-1.2f,0f,1f)
        0.82f at 7500 with LinearOutSlowInEasing
    })
}

fun Animatable<Float, AnimationVector1D>.toUiState(maxSpeed: Float) = UiState(
    actValue = value,
    speed = "%.1f".format(value*100) ,
    ping = if (value > 0.2f) "${(value*15).roundToInt()} ms" else "-",
    maxSpeed = if (maxSpeed > 0f) "%.1f mbps".format(maxSpeed) else "-",
    inProgress = isRunning
)

@Composable
private fun SpeedometerScreen(state: UiState,onClick:() -> Unit){

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Header()
        SpeedIndicator(state = state,onClick = onClick)
        AdditionalInfo(ping = state.ping, maxSpeed = state.maxSpeed)
        NavigationView()
    }
}

@Composable
fun SpeedIndicator(state: UiState,onClick:() -> Unit){
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ){
        CircularSpeedIndicator(state.actValue,240f)
        StartButton( !state.inProgress, onClick)
        SpeedValue(value = state.speed)
    }
}

@Composable
fun SpeedValue(value:String){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "DOWNLOAD", style = MaterialTheme.typography.caption)
        Text(
            text = value,
            color = Color.White,
            fontSize = 45.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = "DOWNLOAD", style = MaterialTheme.typography.caption)
    }
}

@Composable
fun CircularSpeedIndicator(value:Float, angle:Float){
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp)
    ){
        drawLines(value,angle)
        drawArcs(value,angle)
    }
}

fun DrawScope.drawLines(progress:Float,maxValue:Float,numberOfLines:Int = 40){
    val oneRotation = maxValue / numberOfLines
    val startValue = if (progress == 0f) 0 else floor(progress * numberOfLines).toInt()

    for (i in startValue .. numberOfLines){
        rotate(i * oneRotation + (180 - maxValue) / 2 ){
            drawLine(
                Color.Green,
                Offset(if (i % 5 == 0) 80f else 30f, size.height / 2),
                Offset(0f , size.height / 2),
                8f,
                StrokeCap.Round
            )
        }
    }
}

fun DrawScope.drawArcs(progress:Float,maxValue:Float){
    val startAngle = 270 - maxValue / 2
    val sweepAngle = maxValue * progress

    val topLeft = Offset(50f,50f)
    val size = Size(size.width - 100f,size.width - 100f)

    fun drawBlur(){
        for (i in 0..20){
            drawArc(
                color = Color.Green.copy(alpha = i / 900f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = Stroke(width = 80f + (20 - i)*20, cap = StrokeCap.Round)
            )
        }
    }

    fun drawStroke(){
        drawArc(
            color = Color.White,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 86f, cap = StrokeCap.Round)
        )
    }

    fun drawGradient(){
        drawArc(
            color = Color.Blue,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 80f, cap = StrokeCap.Round)
        )
    }

    drawBlur()
    drawStroke()
    drawGradient()

}

@Composable
fun AdditionalInfo(ping:String,maxSpeed:String){

    @Composable
    fun RowScope.InfoColumn(title:String,value: String){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
          Text(text = title)
          Text(
              text = value,
              style = MaterialTheme.typography.body2,
              modifier = Modifier.padding(vertical = 8.dp)
          )
        }
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min))
    {
        InfoColumn(title = "PING", value = ping )
        VerticalDivider()
        InfoColumn(title = "MAX SPEED", value = maxSpeed )
    }

}

@Composable
fun VerticalDivider(){
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color(0xFF414D66))
            .width(1.dp)
    )
}

@Composable
fun NavigationView() {
    val items = listOf(
        R.drawable.wifi,
        R.drawable.person,
        R.drawable.speed,
        R.drawable.settings
    )

    val selectedItem = 2

    BottomNavigation(backgroundColor = Color.DarkGray) {
        items.mapIndexed { index, item ->
            BottomNavigationItem(
                selected = index == selectedItem,
                onClick = { /*TODO*/ },
                selectedContentColor = Color.Green,
                unselectedContentColor = MaterialTheme.colors.onSurface,
                icon = {
                    Icon(painter = painterResource(id = item), contentDescription = null)
                }
            )
        }
    }
}

@Composable
fun Header() {
    Text(
        text = "SPEEDOMETER",
        modifier = Modifier.padding(top = 52.dp, bottom = 16.dp),
        style = MaterialTheme.typography.h6
    )
}

@Composable
fun StartButton(isEnabled:Boolean, onClick:()-> Unit){
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.padding(bottom = 24.dp),
        enabled = isEnabled,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(width = 2.dp,color= MaterialTheme.colors.onSurface)
    ) {
        Text(
            text = "START",
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 4.dp)
        )
    }
}
