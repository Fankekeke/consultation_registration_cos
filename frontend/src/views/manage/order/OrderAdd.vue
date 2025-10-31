<template>
  <a-drawer
    title="添加平台订单"
    :maskClosable="false"
    width=1150
    placement="right"
    :closable="false"
    @close="onClose"
    :visible="orderAddShow"
    style="height: calc(100% - 55px);overflow: auto;padding-bottom: 53px;">
    <a-form :form="form" layout="vertical">
      <a-row :gutter="10">
        <a-divider orientation="left">
          <span style="font-size: 15px;font-weight: bold;">药品选购</span>
        </a-divider>
        <a-col :span="24">
          <a-table :columns="columns" :data-source="dataList" :pagination="false">
            <template slot="nameShow" slot-scope="text, record">
              <a-select style="width: 100%" @change="handleChange($event, record)">
                <a-select-option v-for="(item, index) in drugList" :key="index" :value="item.drugId">{{
                    item.drugName
                  }}
                </a-select-option>
              </a-select>
            </template>
            <template slot="brandShow" slot-scope="text, record">
              <span>{{ record.brand }}</span>
            </template>
            <template slot="typeIdShow" slot-scope="text, record">
              <span v-if="record.classification == 1">中药材</span>
              <span v-if="record.classification == 2">中药饮片</span>
              <span v-if="record.classification == 3">中西成药</span>
              <span v-if="record.classification == 4">化学原料药</span>
              <span v-if="record.classification == 5">抗生素</span>
              <span v-if="record.classification == 6">生化药品</span>
              <span v-if="record.classification == 7">放射性药品</span>
              <span v-if="record.classification == 8">血清</span>
              <span v-if="record.classification == 9">诊断药品</span>
            </template>
            <template slot="dosageFormShow" slot-scope="text, record">
              <span>{{ record.dosageForm }}</span>
            </template>
            <template slot="reserveShow" slot-scope="text, record">
              <a-input-number v-model="record.quantity" :min="1" :max="record.reserve" :step="1"/>
            </template>
            <template slot="priceShow" slot-scope="text, record">
              <span>{{ record.unitPrice }}元</span>
            </template>
          </a-table>
          <a-button @click="dataAdd" type="primary" ghost style="margin-top: 10px;width: 100%">
            新增物品
          </a-button>
        </a-col>
      </a-row>
    </a-form>
    <div class="drawer-bootom-button">
      <a-popconfirm title="确定放弃编辑？" @confirm="onClose" okText="确定" cancelText="取消">
        <a-button style="margin-right: .8rem">取消</a-button>
      </a-popconfirm>
      <a-button @click="handleSubmit" type="primary" :loading="loading">提交</a-button>
    </div>
  </a-drawer>
</template>

<script>
import baiduMap from '@/utils/map/baiduMap'
import drawerMap from '@/utils/map/searchmap/drawerMap'
import {mapState} from 'vuex'

function getBase64 (file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = error => reject(error)
  })
}

const formItemLayout = {
  labelCol: {span: 24},
  wrapperCol: {span: 24}
}
export default {
  name: 'inventoryAdd',
  props: {
    orderAddShow: {
      default: false
    }
  },
  components: {
    drawerMap
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    }),
    show: {
      get: function () {
        return this.orderAddShow
      },
      set: function () {
      }
    },
    columns () {
      return [{
        title: '药品名称',
        dataIndex: 'drugName',
        scopedSlots: {customRender: 'nameShow'}
      }, {
        title: '数量',
        dataIndex: 'quantity',
        scopedSlots: {customRender: 'reserveShow'}
      }, {
        title: '所属品牌',
        dataIndex: 'brand',
        scopedSlots: {customRender: 'brandShow'}
      }, {
        title: '药品类别',
        dataIndex: 'classification',
        scopedSlots: {customRender: 'typeIdShow'}
      }, {
        title: '剂型',
        dataIndex: 'dosageForm',
        scopedSlots: {customRender: 'dosageFormShow'}
      }, {
        title: '单价',
        dataIndex: 'unitPrice',
        scopedSlots: {customRender: 'priceShow'}
      }]
    }
  },
  watch: {
    'orderAddShow': function (value) {
      if (value) {
        this.dataList = []
      }
    }
  },
  data () {
    return {
      formItemLayout,
      form: this.$form.createForm(this),
      loading: false,
      fetching: false,
      fileList: [],
      previewVisible: false,
      previewImage: '',
      hospitalList: [],
      stayAddress: '',
      childrenDrawer: false,
      pharmacyList: [],
      pharmacyInfo: null,
      dataList: [],
      drugList: [],
      staffList: [],
      staffCode: ''
    }
  },
  mounted () {
    this.getDrug(1043)
  },
  methods: {
    handleChange (value, record) {
      console.log(value)
      if (value) {
        this.drugList.forEach(e => {
          if (e.drugId === value) {
            record.brand = e.brand
            record.classification = e.classification
            record.dosageForm = e.dosageForm
            record.unitPrice = e.unitPrice
            record.reserve = e.reserve
            record.drugId = e.drugId
            console.log(record)
          }
        })
      }
    },
    dataAdd () {
      this.dataList.push({drugId: null, quantity: 1, brand: '', classification: '', dosageForm: '', unitPrice: ''})
    },
    filterOption (input, option) {
      return (
        option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
      )
    },
    hospitalCheck (value) {
      if (value) {
        this.hospitalList.forEach(e => {
          if (e.id === value) {
            this.pharmacyInfo = e
            this.getStaff(e.id)
          }
        })
      }
    },
    getStaff (pharmacyId) {
      this.$get(`/cos/staff-info/selectStaffByHospital/${pharmacyId}`).then((r) => {
        this.staffList = r.data.data
      })
    },
    getDrug (pharmacyId) {
      this.$get(`/cos/pharmacy-inventory/detail/pharmacy/${pharmacyId}`).then((r) => {
        this.drugList = r.data.data
      })
    },
    getPharmacy () {
      this.$get('/cos/pharmacy-info/list').then((r) => {
        this.pharmacyList = r.data.data
      })
    },
    showChildrenDrawer () {
      this.childrenDrawer = true
    },
    onChildrenDrawerClos () {
      this.childrenDrawer = false
    },
    handleCancel () {
      this.previewVisible = false
    },
    async handlePreview (file) {
      if (!file.url && !file.preview) {
        file.preview = await getBase64(file.originFileObj)
      }
      this.previewImage = file.url || file.preview
      this.previewVisible = true
    },
    picHandleChange ({fileList}) {
      this.fileList = fileList
    },
    reset () {
      this.loading = false
      this.form.resetFields()
    },
    onClose () {
      this.reset()
      this.$emit('close')
    },
    handleSubmit () {
      this.form.validateFields((err, values) => {
        values.orderDetailList = JSON.stringify(this.dataList)
        values.pharmacyId = 1043
        if (!err) {
          this.loading = true
          this.$post('/cos/order-info/platform', {
            ...values
          }).then((r) => {
            this.reset()
            this.$emit('success')
          }).catch(() => {
            this.loading = false
          })
        }
      })
    }
  }
}
</script>

<style scoped>

</style>
