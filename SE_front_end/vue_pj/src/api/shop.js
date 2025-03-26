import request from '@/utils/request'

// 搜索商家
export function searchShops(params) {
  return request({
    url: '/shops/search',
    method: 'get',
    params: {
      keyword: params.keyword,
      page: params.page || 1,
      size: params.size || 10,
      sortType: params.sortType,
      rating: params.ratings,
      priceRange: params.priceRange,
      averagePrice: params.averagePrice,
      features: params.features
    }
  })
}

// 获取商家详情
export function getShopDetail(id) {
  return request({
    url: `/shops/${id}`,
    method: 'get'
  })
}

// 获取商家评分列表
export function getShopRatings(id) {
  return request({
    url: `/shops/${id}/ratings`,
    method: 'get'
  })
}

// 获取商家服务列表
export function getShopServices(id) {
  return request({
    url: `/shops/${id}/services`,
    method: 'get'
  })
}

// 获取商家分类列表
export function getShopCategories() {
  return request({
    url: '/shops/categories',
    method: 'get'
  })
}

// 获取商家标签列表
export function getShopTags() {
  return request({
    url: '/shops/tags',
    method: 'get'
  })
} 