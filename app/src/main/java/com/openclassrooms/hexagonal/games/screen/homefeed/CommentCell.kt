package com.openclassrooms.hexagonal.games.screen.homefeed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.domain.model.PostComment
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@Composable
fun CommentCell(
    modifier: Modifier = Modifier,
    commentP : PostComment
) {

    // Display the Post
    ElevatedCard(
        modifier = modifier.wrapContentSize(),
    ){
        Column(
            modifier = Modifier
            //.fillMaxSize()
            //.weight(1f)
        ) {

            Text(
                text = stringResource(
                    id = R.string.by,
                    commentP.author?.firstname ?: ""
                ),
                style = MaterialTheme.typography.titleSmall
            )


            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                text = commentP.comment
            )

        }
    }

}

@Preview
@Composable
private fun PostDetailScreenScaffoldLoadingPreview() {


    HexagonalGamesTheme {

        val author = User("1", "JG")
        val postComment = PostComment(id="1",author = author, comment = "Mon commentaire")

        CommentCell(commentP = postComment)

    }
}