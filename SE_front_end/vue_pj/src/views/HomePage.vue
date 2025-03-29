<template>
  <div id="app">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="search-bar" @click="goToSearch">
        <el-input
          placeholder="搜索餐厅、美食..."
          class="search-input"
          readonly
        >
          <template #prefix>
            <el-icon><search /></el-icon>
          </template>
        </el-input>
      </div>
      
      <div class="user-info" v-if="isLoggedIn">
        <el-avatar :size="40" :src="userAvatar" />
        <span class="username">{{ username }}</span>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 左侧内容列 -->
      <div class="content-column left-column">
        <div class="content-card" v-for="(item, index) in leftColumnItems" :key="'left-'+index" @click="goToShopDetail(item.id)">
          <img :src="item.image" class="card-image" />
          <div class="card-content">
            <h3>{{ item.title }}</h3>
            <div class="category-tag" v-if="item.categoryName">{{ item.categoryName }}</div>
            <p>{{ item.description }}</p>
            <div class="rating">
              <el-rate v-model="item.rating" disabled show-score />
            </div>
          </div>
        </div>
        <div class="load-more" @click="viewAllShops">
          <el-button type="text">查看更多商家</el-button>
        </div>
      </div>
      
      <!-- 右侧内容列 -->
      <div class="content-column right-column">
        <div class="content-card" v-for="(item, index) in rightColumnItems" :key="'right-'+index" @click="goToShopDetail(item.id)">
          <img :src="item.image" class="card-image" />
          <div class="card-content">
            <h3>{{ item.title }}</h3>
            <div class="category-tag" v-if="item.categoryName">{{ item.categoryName }}</div>
            <p>{{ item.description }}</p>
            <div class="rating">
              <el-rate v-model="item.rating" disabled show-score />
            </div>
          </div>
        </div>
        <div class="load-more" @click="viewAllShops">
          <el-button type="text">查看更多商家</el-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { Search } from '@element-plus/icons-vue'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AuthService from '@/services/AuthService'
import ShopService from '@/services/ShopService'

export default {
  components: {
    Search
  },
  setup() {
    const router = useRouter();
    
    // 用户信息
    const isLoggedIn = ref(false);
    const username = ref('');
    const userAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png');
    
    // 商家数据
    const leftColumnItems = ref([]);
    const rightColumnItems = ref([]);
    
    // 不同类型商家对应的图片
    const categoryImages = {
      '火锅': 'https://fuss10.elemecdn.com/a/3f/3302e58f9a181d2509f3dc0fa68b0jpeg.jpeg',
      '奶茶': 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
      '烧烤': 'https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png',
      '西餐': 'https://fuss10.elemecdn.com/1/34/19aa98b1fcb2781c4fba33d850549jpeg.jpeg',
      '中餐': 'https://fuss10.elemecdn.com/0/6f/e35ff375812e6b0020b6b4e8f9583jpeg.jpeg',
      '快餐': 'https://fuss10.elemecdn.com/9/bb/e27858e973f5d7d3904835f46abbdjpeg.jpeg',
      '甜品': 'https://fuss10.elemecdn.com/d/e6/c4d93a3805b3ce3f323f7974e6f78jpeg.jpeg',
      '小吃': 'https://fuss10.elemecdn.com/3/28/bbf893f792f03a54408b3b7a7ebf0jpeg.jpeg',
      // 默认图片
      'default': 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg'
    };
    
    // 获取商家图片
    const getShopImage = (shop) => {
      // 根据商家分类确定图片
      const categoryName = shop.categoryName || shop.category?.name;
      return categoryImages[categoryName] || categoryImages.default;
    };
    
    // 加载用户信息
    const loadUserInfo = () => {
      const user = AuthService.getUser();
      isLoggedIn.value = !!user;
      if (user) {
        username.value = user.username || '用户';
      }
    };
    
    // 加载推荐商家
    const loadRecommendedShops = async () => {
      try {
        const response = await ShopService.getRecommendedShops(8);
        if (response.code === 1 && response.data) {
          const shops = response.data;
          
          // 分割商家数据到左右两列
          const half = Math.ceil(shops.length / 2);
          
          // 处理左侧商家信息
          leftColumnItems.value = shops.slice(0, half).map(item => {
            const shop = item.shop;
            const images = item.images || [];
            
            // 优先使用数据库中的图片，如果没有则使用分类对应的默认图片
            let imageUrl = categoryImages.default;
            if (images && images.length > 0) {
              // 使用第一张图片作为封面
              imageUrl = images[0].imageUrl;
            } else if (shop.categoryName || (shop.category && shop.category.name)) {
              // 使用分类对应的默认图片
              const categoryName = shop.categoryName || shop.category?.name;
              imageUrl = categoryImages[categoryName] || categoryImages.default;
            }
            
            return {
              id: shop.id,
              image: imageUrl,
              title: shop.name,
              description: shop.description || 
                `地址：${shop.address} | 营业时间：${shop.businessHours}`,
              rating: parseFloat(shop.rating) || 4.0,
              categoryName: shop.categoryName || shop.category?.name || '未分类'
            };
          });
          
          // 处理右侧商家信息
          rightColumnItems.value = shops.slice(half).map(item => {
            const shop = item.shop;
            const images = item.images || [];
            
            // 优先使用数据库中的图片，如果没有则使用分类对应的默认图片
            let imageUrl = categoryImages.default;
            if (images && images.length > 0) {
              // 使用第一张图片作为封面
              imageUrl = images[0].imageUrl;
            } else if (shop.categoryName || (shop.category && shop.category.name)) {
              // 使用分类对应的默认图片
              const categoryName = shop.categoryName || shop.category?.name;
              imageUrl = categoryImages[categoryName] || categoryImages.default;
            }
            
            return {
              id: shop.id,
              image: imageUrl,
              title: shop.name,
              description: shop.description || 
                `地址：${shop.address} | 营业时间：${shop.businessHours}`,
              rating: parseFloat(shop.rating) || 4.0,
              categoryName: shop.categoryName || shop.category?.name || '未分类'
            };
          });
        }
      } catch (error) {
        console.error('加载推荐商家失败:', error);
        // 加载失败时使用默认数据
        setDefaultShops();
      }
    };
    
    // 设置默认商家数据
    const setDefaultShops = () => {
      leftColumnItems.value = [
        {
          image: 'https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png',
          title: '软早餐厅旗舰店',
          description: '提供各种美味早餐，环境优雅，服务周到',
          rating: 4.7
        },
        {
          image: 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
          title: '早茶时光',
          description: '传统广式早茶，点心种类丰富',
          rating: 4.5
        }
      ];
      rightColumnItems.value = [
        {
          image: 'https://fuss10.elemecdn.com/a/3f/3302e58f9a181d2509f3dc0fa68b0jpeg.jpeg',
          title: '阳光早餐屋',
          description: '西式早餐为主，咖啡品质优良',
          rating: 4.3
        },
        {
          image: 'https://fuss10.elemecdn.com/1/34/19aa98b1fcb2781c4fba33d850549jpeg.jpeg',
          title: '老街包子铺',
          description: '传统手工包子，老字号品牌',
          rating: 4.8
        }
      ];
    };
    
    // 点击"加载更多"按钮
    const loadMore = (column) => {
      console.log(`加载更多${column === 'left' ? '左侧' : '右侧'}内容`);
      // 这里可以调用API加载更多商家
    };
    
    // 查看所有商家
    const viewAllShops = () => {
      router.push('/shop/list');
    };
    
    // 跳转到搜索页面
    const goToSearch = () => {
      router.push('/shop/search');
    };
    
    // 跳转到商店详情页面
    const goToShopDetail = (id) => {
      if (!id) return;
      router.push(`/shop/detail/${id}`);
    };
    
    // 初始化
    onMounted(async () => {
      loadUserInfo();
      await loadRecommendedShops();
    });
    
    return {
      isLoggedIn,
      username,
      userAvatar,
      leftColumnItems,
      rightColumnItems,
      loadMore,
      viewAllShops,
      goToSearch,
      goToShopDetail
    };
  }
}
</script>

<style scoped>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  color: #2c3e50;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.search-bar {
  flex: 1;
  max-width: 500px;
  cursor: pointer;
}

.search-input {
  width: 100%;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-weight: bold;
}

.main-content {
  display: flex;
  gap: 20px;
}

.content-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.content-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
  position: relative;
}

.content-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.card-content {
  padding: 15px;
}

.card-content h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.card-content p {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.rating {
  display: flex;
  align-items: center;
}

.load-more {
  text-align: center;
  padding: 15px;
  cursor: pointer;
}

.category-tag {
  display: inline-block;
  background-color: #ecf5ff;
  color: #409EFF;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-bottom: 8px;
  border: 1px solid #d9ecff;
}

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }
}
</style>
