package com.example.artsy_mobile_app

import android.util.Log

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle


@Serializable
data class CategoriesResponse(
    val statusCode: Int,
    val data: List<Categories>,
    val message: String,
    val success: Boolean
)

@Serializable
data class Categories(
    @SerialName("name") val name: String,
    val thumbnail_href: String? = null,
    val description: String? = null
)

sealed class CategoriesState {
    object Loading : CategoriesState()
    data class Success(val results: List<Categories>) : CategoriesState()
    data class Error(val message: String) : CategoriesState()
}

class CategoriesViewModel : ViewModel() {
    var categoriesState by mutableStateOf<CategoriesState>(CategoriesState.Loading)

    private val client = HttpClientProvider.client

    suspend fun fetchCategories(artworkId: String) {
        categoriesState = CategoriesState.Loading
        val results = fetchCategoriesResultsFromAPI(artworkId)
        categoriesState = if (results.isNotEmpty()) {
            CategoriesState.Success(results)
        } else {
            CategoriesState.Error("No results found")
        }
    }

    private suspend fun fetchCategoriesResultsFromAPI(artworkId: String): List<Categories> {
        val url =
            "https://artsy-shrey-3.wl.r.appspot.com/api/genes?artwork_id=${artworkId}"
        return try {
            val response: String = client.get(url).bodyAsText()
            Log.d("CategoryScreen", "CategoryResponse: $response")

            val categoriesResponse = json.decodeFromString<CategoriesResponse>(response)
            categoriesResponse.data
        } catch (e: Exception) {
            Log.e("GetGenesAPIError", "Error fetching Categories results: ${e.message}")
            emptyList()
        }
    }
}

@Composable
fun formatLatexText(text: String): AnnotatedString {
    return buildAnnotatedString {
        var currentPos = 0
        val linkRegex = Regex("\\[(.*?)]\\(.*?\\)")


        linkRegex.findAll(text).forEach { match ->
            append(text.substring(currentPos, match.range.first))
            append(match.groupValues[1])
            currentPos = match.range.last + 1
        }

        if (currentPos < text.length) {
            val remainingText = text.substring(currentPos)

            val parts = remainingText.split(Regex("([*_/])"))
            var isBold = false
            var isItalic = false
            var isUnderline = false

            for (part in parts) {
                when (part) {
                    "*" -> isBold = !isBold
                    "_" -> isItalic = !isItalic
                    "/" -> isUnderline = !isUnderline
                    else -> {
                        withStyle(
                            SpanStyle(
                                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                            )
                        ) {
                            append(part)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesCarousel(
    categories: List<Categories>,
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { categories.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pager in the center
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        ) { page ->
            val category = categories[page]
            val scrollState = rememberScrollState()
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    AsyncImage(
                        model = category.thumbnail_href,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .height(150.dp)
                            .fillMaxWidth()
                    )
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .padding(top = 16.dp)
                    )
                    Text(
                        text = formatLatexText(category.description ?: ""),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(top = 16.dp)
                    )
                }
            }
        }

        // Left Arrow
        IconButton(
            onClick = {
                scope.launch {
                    val prevPage = if (pagerState.currentPage > 0){
                        pagerState.currentPage - 1
                    } else {
                        categories.lastIndex
                    }
                    pagerState.animateScrollToPage(prevPage)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .absoluteOffset(x = (-16).dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
        }

        // Right Arrow
        IconButton(
            onClick = {
                scope.launch {
                    val nextPage = if (pagerState.currentPage < categories.lastIndex) {
                        pagerState.currentPage + 1
                    } else {0}
                    pagerState.animateScrollToPage(nextPage)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .absoluteOffset(x = 16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
        }
    }
}
