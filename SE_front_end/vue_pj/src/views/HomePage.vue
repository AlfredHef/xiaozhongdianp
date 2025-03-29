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
        <div class="content-card" v-for="(item, index) in leftColumnItems" :key="'left-'+index">
          <img :src="item.image" class="card-image" />
          <div class="card-content">
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
            <div class="rating">
              <el-rate v-model="item.rating" disabled show-score />
            </div>
          </div>
        </div>
        <div class="load-more" @click="loadMore('left')">
          <el-button type="text">加载更多</el-button>
        </div>
      </div>
      
      <!-- 右侧内容列 -->
      <div class="content-column right-column">
        <div class="content-card" v-for="(item, index) in rightColumnItems" :key="'right-'+index">
          <img :src="item.image" class="card-image" />
          <div class="card-content">
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
            <div class="rating">
              <el-rate v-model="item.rating" disabled show-score />
            </div>
          </div>
        </div>
        <div class="load-more" @click="loadMore('right')">
          <el-button type="text">加载更多</el-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { Search } from '@element-plus/icons-vue'

export default {
  components: {
    Search
  },
  data() {
    return {
      isLoggedIn: true, // 根据实际登录状态修改
      username: '软早用户',
      userAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      leftColumnItems: [
        {
          image: 'https://via.placeholder.com/300x200?text=餐厅1',
          title: '软早餐厅旗舰店',
          description: '提供各种美味早餐，环境优雅，服务周到',
          rating: 4.7
        },
        {
          image: 'https://via.placeholder.com/300x200?text=餐厅2',
          title: '早茶时光',
          description: '传统广式早茶，点心种类丰富',
          rating: 4.5
        }
      ],
      rightColumnItems: [
        {
          image: 'https://via.placeholder.com/300x200?text=餐厅3',
          title: '阳光早餐屋',
          description: '西式早餐为主，咖啡品质优良',
          rating: 4.3
        },
        {
          image: 'https://via.placeholder.com/300x200?text=餐厅4',
          title: '老街包子铺',
          description: '传统手工包子，老字号品牌',
          rating: 4.8
        }
      ]
    }
  },
  methods: {
    loadMore(column) {
      // 这里可以添加加载更多数据的逻辑
      console.log(`加载更多${column === 'left' ? '左侧' : '右侧'}内容`)
    },
    goToSearch() {
      this.$router.push('/shop/search');
      setTimeout(() => {
        // 聚焦到搜索框
        const searchInput = document.querySelector('input[placeholder="搜索商家，如\'火锅\'、\'奶茶\'、\'炸鸡\'"]');
        if (searchInput) {
          searchInput.focus();
        }
      }, 500); // 延时保证页面渲染完成
    }
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
  transition: transform 0.3s;
}

.content-card:hover {
  transform: translateY(-5px);
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

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }
}
</style>
