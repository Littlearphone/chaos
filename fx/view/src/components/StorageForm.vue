<template>
  <div v-loading="operating">
    <el-dialog title="存储配置" v-model="visible" custom-class="storage-form-dialog">
      <el-form ref="form" :model="formData" label-width="80px">
        <el-form-item label="存储名称">
          <el-input v-model="formData.title"></el-input>
        </el-form-item>
        <el-form-item label="存储类型">
          <el-select v-model="formData.type" placeholder="请选择存储类型">
            <el-option label="本地存储" value="fs"></el-option>
            <el-option label="SMB共享" value="smb"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="存储路径">
          <el-input v-model="formData.path"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="visible = false">取 消</el-button>
          <el-button type="primary" @click="saveStorage">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script>
import { saveStorage } from '../api'
import { ElMessage } from 'element-plus'

export default {
  props: {
    storageForm: {
      type: Object,
      default: () => ({})
    },
    formVisible: {
      type: Number,
      default: 0
    }
  },
  watch: {
    storageForm: function(value) {
      this.formData = value
    },
    formVisible: function(value) {
      this.visible = value > 0
      this.operating = false
    }
  },
  data() {
    return {
      operating: false,
      visible: false,
      formData: {}
    }
  },
  methods: {
    saveStorage() {
      this.operating = true
      saveStorage(this.formData).then(response => response.data).then(data => {
        this.formData = {}
        this.visible = false
        this.operating = false
        this.$bus.$emit('saveStorage', { ...data })
        ElMessage({
          type: 'success',
          showClose: true,
          message: '添加存储成功'
        })
      }).catch(() => (this.operating = false))
    }
  }
}
</script>
<style lang="less">
div.storage-form-dialog {
  min-width: 360px;
}
</style>
