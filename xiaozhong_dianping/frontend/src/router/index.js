import MyCoupons from '@/views/coupon/MyCoupons.vue';

{
  path: '/my-coupons',
  name: 'MyCoupons',
  component: MyCoupons,
  meta: {
    requiresAuth: true
  }
}, 