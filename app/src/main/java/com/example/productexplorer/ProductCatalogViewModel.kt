package com.example.productexplorer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SortOption(val label: String) {
    NONE("Aucun tri"),
    PRICE_ASCENDING("Prix croissant"),
    PRICE_DESCENDING("Prix décroissant")
}

data class ProductCatalogUiState(
    val products: List<ProductUi> = emptyList(),
    val categories: List<String> = emptyList(),
    val searchQuery: String = "",
    val showOnlyInStock: Boolean = false,
    val showOnlyFavorites: Boolean = false,
    val sortOption: SortOption = SortOption.NONE,
    val favoriteProductIds: List<Int> = emptyList()
)

class ProductCatalogViewModel : ViewModel() {

    private val allProducts: List<ProductUi> = sampleProducts()
    private val allCategories: List<String> = sampleCategories()

    private val _uiState = MutableStateFlow(
        ProductCatalogUiState(
            products = allProducts,
            categories = allCategories
        )
    )
    val uiState: StateFlow<ProductCatalogUiState> = _uiState.asStateFlow()

    private fun filterProducts(
        searchQuery: String,
        showOnlyInStock: Boolean,
        showOnlyFavorites: Boolean,
        favoriteProductIds: List<Int>,
        sortOption: SortOption
    ): List<ProductUi> {
        val filtered = allProducts.filter { product ->
            val matchesSearch =
                product.title.contains(searchQuery, ignoreCase = true) ||
                    product.brand.contains(searchQuery, ignoreCase = true) ||
                    product.category.contains(searchQuery, ignoreCase = true)

            val matchesStock =
                !showOnlyInStock || product.stock > 0

            val matchesFavorite =
                !showOnlyFavorites || favoriteProductIds.contains(product.id)

            matchesSearch && matchesStock && matchesFavorite
        }

        return when (sortOption) {
            SortOption.NONE -> filtered
            SortOption.PRICE_ASCENDING -> filtered.sortedBy { product -> product.price }
            SortOption.PRICE_DESCENDING -> filtered.sortedByDescending { product -> product.price }
        }
    }

    fun onSearchQueryChange(newValue: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = newValue,
                products = filterProducts(
                    searchQuery = newValue,
                    showOnlyInStock = currentState.showOnlyInStock,
                    showOnlyFavorites = currentState.showOnlyFavorites,
                    favoriteProductIds = currentState.favoriteProductIds,
                    sortOption = currentState.sortOption
                )
            )
        }
    }

    fun onToggleStockFilter() {
        _uiState.update { currentState ->
            val newShowOnlyInStock = !currentState.showOnlyInStock

            currentState.copy(
                showOnlyInStock = newShowOnlyInStock,
                products = filterProducts(
                    searchQuery = currentState.searchQuery,
                    showOnlyInStock = newShowOnlyInStock,
                    showOnlyFavorites = currentState.showOnlyFavorites,
                    favoriteProductIds = currentState.favoriteProductIds,
                    sortOption = currentState.sortOption
                )
            )
        }
    }

    fun onToggleFavoritesFilter() {
        _uiState.update { currentState ->
            val newShowOnlyFavorites = !currentState.showOnlyFavorites

            currentState.copy(
                showOnlyFavorites = newShowOnlyFavorites,
                products = filterProducts(
                    searchQuery = currentState.searchQuery,
                    showOnlyInStock = currentState.showOnlyInStock,
                    showOnlyFavorites = newShowOnlyFavorites,
                    favoriteProductIds = currentState.favoriteProductIds,
                    sortOption = currentState.sortOption
                )
            )
        }
    }

    fun onSortOptionChange(newSortOption: SortOption) {
        _uiState.update { currentState ->
            currentState.copy(
                sortOption = newSortOption,
                products = filterProducts(
                    searchQuery = currentState.searchQuery,
                    showOnlyInStock = currentState.showOnlyInStock,
                    showOnlyFavorites = currentState.showOnlyFavorites,
                    favoriteProductIds = currentState.favoriteProductIds,
                    sortOption = newSortOption
                )
            )
        }
    }

    fun onFavoriteClick(productId: Int) {
        _uiState.update { currentState ->
            val newFavoriteIds =
                if (currentState.favoriteProductIds.contains(productId)) {
                    currentState.favoriteProductIds - productId
                } else {
                    currentState.favoriteProductIds + productId
                }

            currentState.copy(
                favoriteProductIds = newFavoriteIds,
                products = filterProducts(
                    searchQuery = currentState.searchQuery,
                    showOnlyInStock = currentState.showOnlyInStock,
                    showOnlyFavorites = currentState.showOnlyFavorites,
                    favoriteProductIds = newFavoriteIds,
                    sortOption = currentState.sortOption
                )
            )
        }
    }
}
