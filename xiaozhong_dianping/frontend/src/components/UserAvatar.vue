<template>
  <div class="user-avatar">
    <el-dropdown @command="handleCommand">
      <span class="el-dropdown-link">
        <el-avatar :size="32" :src="userAvatar"></el-avatar>
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="profile">个人中心</el-dropdown-item>
          <el-dropdown-item command="coupons">我的券包</el-dropdown-item>
          <el-dropdown-item command="orders">我的订单</el-dropdown-item>
          <el-dropdown-item command="logout">退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script>
export default {
  name: 'UserAvatar',
  data() {
    return {
      userAvatar: localStorage.getItem('userAvatar') || ''
    };
  },
  methods: {
    handleCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push('/profile');
          break;
        case 'coupons':
          this.$router.push('/my-coupons');
          break;
        case 'orders':
          this.$router.push('/orders');
          break;
        case 'logout':
          this.handleLogout();
          break;
      }
    },
    handleLogout() {
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      localStorage.removeItem('userAvatar');
      this.$router.push('/login');
    }
  }
};
</script>

<style scoped>
.user-avatar {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}
</style> 