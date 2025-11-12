package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import mobappdev.example.nback_cimpl.ui.viewmodels.GameVM
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.SquareCard
import androidx.compose.ui.graphics.RectangleShape

@Composable
fun AnimatedBox(vm: SquareCard){



}

@Composable
fun MyCard(it: Int, vm: GameVM){
    val squareAnimation = rememberInfiniteTransition(label="infinite animation");
    val morphProgress = squareAnimation.animateFloat(
        initialValue = 0f,
        targetValue=1f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing=LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label="morph"
    );

    val rotationAnimation = rememberInfiniteTransition(label="rotation animation");
    val rotationProgress = rotationAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(10000, easing= LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label="rotate"
    );
    Card(
        modifier = Modifier.size(100.dp).padding(6.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors= CardDefaults.cardColors(containerColor = vm.getSquare(it).color)
    ) {
        Box(
            modifier=Modifier.fillMaxSize().drawWithCache{
                val square = vm.getSquare(it).createSquare(size.minDimension, size.width, size.height);
                val roundedSquare = vm.getSquare(it).createRoundedSquare(size.minDimension, size.width, size.height, 50f, 10f);

                val squareMorph = Morph(start = roundedSquare, end = square);
                val squarePath = squareMorph.toPath(progress = morphProgress.value).asComposePath();


                onDrawBehind {
                    rotate(rotationProgress.value){
                        drawPath(squarePath, color = vm.getSquare(it).secondColor)
                    }
                }
            },
            contentAlignment = Alignment.Center,
        ){
        }
    }
}


@Composable
fun GameScreen(
    vm: GameVM
){
    val gameState by vm.gameState.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally){
        Text(text="Score: ${vm.score.value}", style=MaterialTheme.typography.headlineLarge)
        Text(text="N = ${vm.nBack}", style=MaterialTheme.typography.headlineLarge)
        LazyVerticalGrid(
            columns= GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            items(9){
                MyCard(it, vm);
            }
        }

    }

    Row(
        verticalAlignment = Alignment.Bottom
    ){
        Button(
            onClick={
                vm.checkMatch()
            },
            shape = RectangleShape,
            modifier=Modifier.fillMaxWidth(0.5f).height(200.dp)
        ){
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "SOUND", style = MaterialTheme.typography.titleLarge);
                Icon(
                    painter = painterResource(id = R.drawable.note),
                    contentDescription = "Note",
                    modifier = Modifier
                        .height(48.dp)
                        .aspectRatio(3f / 2f)
                )
            }
        }
        Button(
            onClick={
                vm.checkMatch()
            },
            shape=RectangleShape,
            modifier=Modifier.fillMaxWidth().height(200.dp)
        ){
            val card = SquareCard(vm.darkBlue);
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "POSITION", style = MaterialTheme.typography.titleLarge);
                Icon(
                    painter = painterResource(id = R.drawable.position),
                    contentDescription = "Note",
                    modifier = Modifier
                        .height(48.dp)
                        .aspectRatio(3f / 2f)
                )
            }
        }
    }





    /*if (gameState.eventValue != -1) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Current eventValue is: ${gameState.eventValue}",
                textAlign = TextAlign.Center
            )
        }*/
}