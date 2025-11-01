<template>
  <div class="work-container">
    <div class="header-section" style="background-image: linear-gradient(rgba(0, 0, 0, 0.8), rgba(0, 0, 0, 0.8)), url(../static/img/house.jpg);">
      <div class="header-content">
        <h1 class="main-title">选择科室挂号</h1>
        <p class="sub-title">科室列表</p>
      </div>
    </div>

    <div class="content-section">
      <a-card class="main-card" :bordered="false">
        <a-breadcrumb class="breadcrumb-nav">
          <a-breadcrumb-item
            v-for="(item, index) in menuList"
            :key="index"
            @click.native="changeMenu(item.type)"
            class="breadcrumb-item"
          >
            {{ item.name }}
          </a-breadcrumb-item>
        </a-breadcrumb>

        <div v-if="checkStage == 1" class="list-container">
          <a-list-item slot="renderItem" slot-scope="item, index">
            <a-card hoverable class="hospital-card">
              <div class="image-container">
                <img
                  v-if="item.images"
                  class="hospital-image"
                  alt="医院图片"
                  :src="'http://127.0.0.1:9527/imagesWeb/' + item.images.split(',')[0]"
                />
                <img
                  v-else
                  class="hospital-image"
                  alt="默认图片"
                  src="/static/img/9370437.png"
                />
                <div class="hospital-badge">
                  {{ item.hospitalGrade }}
                </div>
              </div>
              <div class="hospital-content">
                <a-card-meta
                  :title="item.hospitalName"
                  :description="item.hospitalAddress"
                  class="hospital-meta"
                ></a-card-meta>
                <div class="hospital-info">
                  <a-tag color="blue">{{ item.hospitalNature }}</a-tag>
                </div>
                <div class="card-actions">
                  <a-button type="primary" size="small" block @click="selectOfficeList(item)">
                    <a-icon type="apartment" /> 查看科室
                  </a-button>
                  <a-button size="small" block @click="handlehospitalViewOpen(item)">
                    <a-icon type="eye" /> 详情
                  </a-button>
                </div>
              </div>
            </a-card>
          </a-list-item>
        </div>

        <div v-if="checkStage == 2" class="list-container">
          <a-list
            :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
            :pagination="pagination"
            :data-source="officeList"
          >
            <a-list-item slot="renderItem" slot-scope="item, index">
              <a-card hoverable class="office-card">
                <div class="office-card-content">
                  <div class="office-icon">
                    <a-avatar :size="48" icon="medicine-box" />
                  </div>
                  <a-card-meta
                    :title="item.officesName"
                    :description="item.officesDiagnosisScope"
                    class="office-meta"
                  ></a-card-meta>
                  <div class="office-action">
                    <a-button
                      type="primary"
                      size="small"
                      block
                      @click="selectScheduleList(item)"
                    >
                      <a-icon type="calendar" /> 查看排班
                    </a-button>
                  </div>
                </div>
              </a-card>
            </a-list-item>
          </a-list>
        </div>

        <div v-if="checkStage == 3" class="list-container">
          <a-list
            :grid="{ gutter: 16, column: 4 }"
            :pagination="pagination"
            :data-source="scheduleList"
          >
            <a-list-item slot="renderItem" slot-scope="item, index">
              <a-card hoverable class="schedule-card">
                <div class="schedule-header">
                  <a-avatar :size="40" icon="user" class="doctor-avatar" />
                  <div class="doctor-info">
                    <div class="doctor-name">{{ item.doctorName }}</div>
                    <div class="doctor-title">{{ item.name }}</div>
                  </div>
                </div>

                <div class="schedule-content">
                  <div class="schedule-date-section">
                    <div class="schedule-item">
                      <a-icon type="schedule" class="item-icon" />
                      {{ item.taskDate }}
                    </div>
                  </div>

                  <div class="time-range-section">
                    <div class="schedule-item">
                      <a-icon type="clock-circle" class="item-icon" />
                      {{ item.startDate }} ~ {{ item.endDate }}
                    </div>
                  </div>

                  <div class="reservation-status">
                    <a-button
                      v-if="item.registerId == null"
                      type="primary"
                      block
                      @click="reserveDoctor(item)"
                    >
                      <a-icon type="calendar" /> 预约挂号
                    </a-button>
                    <a-button v-else disabled block>
                      <a-icon type="check-circle" /> 已被预约
                    </a-button>
                  </div>
                </div>
              </a-card>
            </a-list-item>
          </a-list>
        </div>
      </a-card>
    </div>

    <hospital-view
      @close="handlehospitalViewClose"
      :hospitalShow="hospitalView.visiable"
      :hospitalData="hospitalView.data">
    </hospital-view>
  </div>
</template>

<script>

import {mapState} from 'vuex'
import hospitalView from './HospitalView.vue'
export default {
  name: 'Work',
  components: {hospitalView},
  data () {
    return {
      menuList: [],
      scheduleList: [],
      checkStage: 2,
      orderMapView: {
        merchantInfo: null,
        visiable: false
      },
      hospitalView: {
        visiable: false,
        data: null
      },
      pagination: {
        onChange: page => {
          console.log(page)
        },
        pageSize: 12
      },
      key: '',
      hospitalList: [],
      officeList: [],
      loading: false,
      rentView: {
        visiable: false,
        data: null
      },
      startDate: null,
      endDate: null
    }
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    })
  },
  mounted () {
    this.selectOfficeList(1043)
  },
  methods: {
    reserveDoctor (item) {
      console.log(item)
      let data = {
        userId: this.currentUser.userId,
        scheduleId: item.id

      }
      this.$post(`/cos/register-info/registerOrderAdd`, data).then((r) => {
        let resultData = r.data.data
        console.log(JSON.stringify(resultData))
        let payData = {
          outTradeNo: resultData.code,
          subject: resultData.code,
          totalPrice: resultData.price
        }
        this.pay(payData)
      })
    },
    pay (row) {
      let data = { outTradeNo: row.outTradeNo, subject: `${row.subject}`, totalAmount: row.totalPrice, body: '' }
      this.$post('/cos/pay/alipay', data).then((r) => {
        // console.log(r.data.msg)
        // 添加之前先删除一下，如果单页面，页面不刷新，添加进去的内容会一直保留在页面中，二次调用form表单会出错
        const divForm = document.getElementsByTagName('div')
        if (divForm.length) {
          document.body.removeChild(divForm[0])
        }
        const div = document.createElement('div')
        div.innerHTML = r.data.msg // data就是接口返回的form 表单字符串
        // console.log(div.innerHTML)
        document.body.appendChild(div)
        document.forms[0].setAttribute('target', '_self') // 新开窗口跳转
        document.forms[0].submit()
      })
    },
    changeMenu (type) {
      if (type === 1) {
        return false
      }
      if (type === 2) {
        this.menuList.pop()
      }
      this.checkStage = type
    },
    selectScheduleList (office) {
      this.$get(`/cos/doctor-info/selectDoctorByOfficeId/${office.id}`).then((r) => {
        this.scheduleList = r.data.data
        this.menuList.push({name: office.officesName, type: 2})
        this.checkStage = 3
      })
    },
    selectOfficeList (hospitalId) {
      this.$get(`/cos/office-info/list/byhospital/${hospitalId}`).then((r) => {
        this.officeList = r.data.data
        this.menuList = [{name: '龙潭社区卫生服务中心', type: 1}]
        this.checkStage = 2
      })
    },
    handlehospitalViewOpen (row) {
      this.hospitalView.data = row
      this.hospitalView.visiable = true
    },
    handlehospitalViewClose () {
      this.hospitalView.visiable = false
    },
    handlevehicleViewClose () {
      this.vehicleView.visiable = false
    },
    handlevehicleViewSuccess () {
      this.vehicleView.visiable = false
      this.$message.success('添加订单成功')
    },
    orderSuccess () {
      this.rentView.visiable = false
      this.$message.success('添加订单成功')
      this.fetch()
    },
    view (record) {
      // if (record.currentStatus === '0') {
      //   this.$message.warn('此商家不在营业时间内')
      //   return false
      // }
      this.orderMapView.merchantInfo = record
      this.orderMapView.visiable = true
    },
    handleorderMapViewClose () {
      this.orderMapView.visiable = false
    },
    getWorkStatusList () {
      this.$get(`/cos/hospital-info/query/like/list`, { key: this.key }).then((r) => {
        this.hospitalList = r.data.data
        console.log(this.hospitalList.length)
      })
    },
    fetch () {
      this.getWorkStatusList()
    }
  }
}
</script>

<style scoped>.work-container {
  background: #f0f2f5;
  min-height: 100vh;
  padding: 20px;
}

.header-section {
  height: 250px;
  background-size: cover;
  background-position: center;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-content {
  text-align: center;
}

.main-title {
  font-size: 36px;
  font-weight: 600;
  color: white;
  font-family: 'Microsoft YaHei', sans-serif;
  margin-bottom: 10px;
}

.sub-title {
  font-size: 24px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  font-family: 'Microsoft YaHei', sans-serif;
}

.content-section {
  margin: 0 auto;
}

.main-card {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.breadcrumb-nav {
  margin-bottom: 20px;
  padding: 10px 0;
  border-bottom: 1px solid #e8e8e8;
}

.breadcrumb-item {
  font-size: 16px;
  cursor: pointer;
}

.breadcrumb-item:hover {
  color: #1890ff;
}

.list-container {
  margin-top: 20px;
}

.hospital-card,
.office-card,
.schedule-card {
  border-radius: 6px;
  transition: all 0.3s ease;
  height: 100%;
}

.hospital-card:hover,
.office-card:hover,
.schedule-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.hospital-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 4px 4px 0 0;
}

.card-actions {
  display: flex;
  justify-content: space-between;
  margin: 15px 0;
}

.card-actions.center {
  justify-content: center;
}

.card-actions .ant-btn {
  margin: 0 5px;
}

.hospital-info {
  margin-top: 10px;
  font-size: 14px;
  color: #666;
  font-family: 'Microsoft YaHei', sans-serif;
}

.schedule-date {
  font-size: 16px;
  font-weight: 500;
  text-align: center;
  margin-bottom: 8px;
  color: #1890ff;
}

.doctor-name {
  font-size: 14px;
  margin-bottom: 5px;
}

.time-range {
  margin-top: 10px;
  font-size: 13px;
  color: #52c41a;
  font-weight: 500;
  text-align: center;
}

>>> .ant-card-meta-title {
  font-size: 16px;
  font-family: 'Microsoft YaHei', sans-serif;
  margin-top: 8px;
  font-weight: 500;
}

>>> .ant-card-meta-description {
  font-size: 14px;
  font-family: 'Microsoft YaHei', sans-serif;
  color: #666;
}

@media (max-width: 768px) {
  .work-container {
    padding: 10px;
  }

  .header-section {
    height: 200px;
  }

  .main-title {
    font-size: 28px;
  }

  .sub-title {
    font-size: 20px;
  }

  .main-card {
    padding: 15px;
  }
}

.image-container {
  position: relative;
  overflow: hidden;
}

.hospital-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.hospital-card:hover .hospital-image {
  transform: scale(1.05);
}

.hospital-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(24, 144, 255, 0.8);
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: bold;
}

.hospital-content {
  padding: 10px 0;
}

.hospital-meta {
  margin-bottom: 10px;
}

.hospital-meta >>> .ant-card-meta-title {
  font-size: 16px !important;
  font-weight: 600 !important;
}

.hospital-meta >>> .ant-card-meta-description {
  font-size: 13px !important;
  color: #666 !important;
}

.hospital-info {
  margin: 10px 0;
}

.card-actions {
  display: flex;
  gap: 8px;
  margin-top: 15px;
}

.card-actions .ant-btn {
  flex: 1;
  border-radius: 4px;
}

.office-card {
  text-align: center;
  transition: all 0.3s ease;
}

.office-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.office-card-content {
  padding: 15px 10px;
}

.office-icon {
  margin-bottom: 15px;
  display: flex;
  justify-content: center;
}

.office-icon .ant-avatar {
  background-color: #e6f7ff;
  color: #1890ff;
}

.office-meta {
  margin-bottom: 15px;
}

.office-meta >>> .ant-card-meta-title {
  font-size: 16px !important;
  font-weight: 600 !important;
  color: #333 !important;
}

.office-meta >>> .ant-card-meta-description {
  font-size: 13px !important;
  color: #666 !important;
  min-height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.office-action .ant-btn {
  border-radius: 4px;
}

.schedule-card {
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.schedule-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.schedule-header {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fafafa;
  border-radius: 6px 6px 0 0;
}

.doctor-avatar {
  background-color: #e6f7ff;
  color: #1890ff;
}

.doctor-info {
  margin-left: 12px;
  flex: 1;
}

.doctor-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.doctor-title {
  font-size: 13px;
  color: #666;
  margin-top: 2px;
}

.schedule-content {
  padding: 15px;
}

.schedule-date-section,
.time-range-section {
  margin-bottom: 15px;
  text-align: center;
}

.schedule-date-section .ant-tag,
.time-range-section .ant-tag {
  padding: 5px 15px;
  font-size: 14px;
}

.reservation-status .ant-btn {
  border-radius: 4px;
}

.reservation-status .ant-btn-primary {
  background-color: #1890ff;
  border-color: #1890ff;
}

.reservation-status .ant-btn[disabled] {
  background-color: #f5f5f5;
  border-color: #d9d9d9;
  color: rgba(0, 0, 0, 0.25);
}

.schedule-item {
  display: flex;
  align-items: center;
  padding: 6px 0;
  font-size: 14px;
  color: #666;
  justify-content: flex-start; /* 明确设置为靠左对齐 */
  text-align: left;
}

.schedule-date-section,
.time-range-section {
  margin-bottom: 15px;
  text-align: left; /* 确保容器也靠左对齐 */
}

.item-icon {
  margin-right: 8px;
  color: #1890ff;
}

.schedule-date-section .item-icon {
  color: #1890ff;
}

.time-range-section .item-icon {
  color: #52c41a;
}
</style>
