<template>
  <div class="system-page">
    <a-row :gutter="16">
      <!-- 左侧部门组织架构筛选树 -->
      <a-col :span="6">
        <a-card :bordered="false" class="page-card" style="height: 100%">
          <div class="dept-tree-header">
            <span style="font-weight: 600; font-size: 15px">组织架构筛选</span>
            <a-button type="link" size="small" @click="selectDept(undefined)">重置全员</a-button>
          </div>
          <a-tree
            v-if="deptTreeData.length"
            :tree-data="deptTreeData"
            default-expand-all
            :selected-keys="selectedDeptKeys"
            style="margin-top: 12px"
            @select="onDeptSelect"
          />
        </a-card>
      </a-col>

      <!-- 右侧员工用户主台账 -->
      <a-col :span="18">
        <a-card :bordered="false" class="page-card">
          <div class="table-header">
            <div class="header-filters">
              <a-space>
                <a-input-search
                  v-model:value="searchKeyword"
                  placeholder="搜索工号、姓名、账号..."
                  enter-button
                  allow-clear
                  style="width: 260px"
                  @search="handleSearch"
                />
                <a-select
                  v-model:value="filterStatus"
                  placeholder="状态筛选"
                  style="width: 120px"
                  allow-clear
                  @change="handleSearch"
                >
                  <a-select-option :value="1">正常在职</a-select-option>
                  <a-select-option :value="0">停用/离职</a-select-option>
                </a-select>
              </a-space>
            </div>
            <div class="header-actions">
              <a-space>
                <a-button type="primary" @click="openCreateModal">
                  <template #icon><PlusOutlined /></template>
                  新增员工账号
                </a-button>
                <a-button @click="loadData">
                  <template #icon><ReloadOutlined /></template>
                  刷新
                </a-button>
              </a-space>
            </div>
          </div>

          <a-table
            :columns="columns"
            :data-source="userList"
            :loading="loading"
            row-key="id"
            style="margin-top: 16px"
            :pagination="{
              current: pageNum,
              pageSize: 10,
              total: total,
              onChange: (p: number) => { pageNum = p; loadData(); }
            }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'realName'">
                <div style="display: flex; align-items: center; gap: 8px">
                  <a-avatar size="small" style="background-color: #1677ff">
                    {{ record.realName ? record.realName.charAt(0) : 'U' }}
                  </a-avatar>
                  <span style="font-weight: 500">{{ record.realName }}</span>
                </div>
              </template>

              <template v-else-if="column.key === 'roleNames'">
                <a-tag
                  v-for="(r, idx) in (record.roleNames && record.roleNames.length ? record.roleNames : ['普通员工'])"
                  :key="idx"
                  color="blue"
                  style="margin-right: 4px"
                >
                  {{ r }}
                </a-tag>
              </template>

              <template v-else-if="column.key === 'status'">
                <a-switch
                  :checked="record.status === 1"
                  checked-children="正常"
                  un-checked-children="停用"
                  @change="(val: any) => handleStatusChange(record.id, val ? 1 : 0)"
                />
              </template>

              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="openEditModal(record)">
                    编辑
                  </a-button>
                  <a-popconfirm
                    title="确定将该用户密码重置为 123456 吗？"
                    @confirm="handleResetPassword(record.id)"
                  >
                    <a-button type="link" size="small">重置密码</a-button>
                  </a-popconfirm>
                  <a-popconfirm
                    title="确定删除该员工账号吗？"
                    @confirm="handleDelete(record.id)"
                  >
                    <a-button type="link" danger size="small">删除</a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>

    <!-- 员工新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalMode === 'create' ? '新增员工账号' : '编辑员工资料'"
      width="640px"
      :confirm-loading="submitting"
      @ok="handleModalSubmit"
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="登录账号" required>
              <a-input
                v-model:value="formData.username"
                placeholder="系统登录账号（英文字母或工号）"
                :disabled="modalMode === 'edit'"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="真实姓名" required>
              <a-input v-model:value="formData.realName" placeholder="例如：张三" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="员工工号">
              <a-input v-model:value="formData.workNo" placeholder="如 OA1008" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="所属组织部门">
              <a-tree-select
                v-model:value="formData.deptId"
                :tree-data="deptSelectOptions"
                placeholder="选择所属部门"
                allow-clear
                tree-default-expand-all
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="岗位职务">
              <a-input v-model:value="formData.postName" placeholder="例如：高级开发工程师" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="分配角色身份">
              <a-select
                v-model:value="formData.roleIds"
                mode="multiple"
                placeholder="可选择多个角色"
                :options="roleSelectOptions"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="手机号码">
              <a-input v-model:value="formData.phone" placeholder="员工手机号" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="电子邮箱">
              <a-input v-model:value="formData.email" placeholder="example@graddu.com" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row v-if="modalMode === 'create'" :gutter="16">
          <a-col :span="24">
            <a-form-item label="初始登录密码">
              <a-input-password
                v-model:value="formData.password"
                placeholder="默认为 123456（可留空使用默认初始密码）"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  PlusOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue';
import {
  getUserPageApi,
  createUserApi,
  updateUserApi,
  resetUserPasswordApi,
  changeUserStatusApi,
  deleteUserApi,
  getDeptTreeApi,
  listRolesApi,
  type DeptItem,
  type RoleItem
} from '@/api/system';
import type { UserVO } from '@/types/user';

const loading = ref(false);
const submitting = ref(false);
const userList = ref<UserVO[]>([]);
const total = ref(0);
const pageNum = ref(1);

const searchKeyword = ref('');
const filterStatus = ref<number | undefined>(undefined);
const selectedDeptId = ref<number | undefined>(undefined);
const selectedDeptKeys = ref<number[]>([]);

const deptTreeData = ref<any[]>([]);
const deptSelectOptions = ref<any[]>([]);
const roleList = ref<RoleItem[]>([]);

const modalVisible = ref(false);
const modalMode = ref<'create' | 'edit'>('create');

const formData = reactive<{
  id?: number;
  username: string;
  realName: string;
  password?: string;
  workNo: string;
  deptId?: number;
  postName: string;
  phone: string;
  email: string;
  roleIds: number[];
}>({
  username: '',
  realName: '',
  password: '',
  workNo: '',
  deptId: undefined,
  postName: '',
  phone: '',
  email: '',
  roleIds: []
});

const columns = [
  { title: '工号', dataIndex: 'workNo', key: 'workNo', width: 100 },
  { title: '员工姓名', dataIndex: 'realName', key: 'realName', width: 140 },
  { title: '登录账号', dataIndex: 'username', key: 'username', width: 120 },
  { title: '所属部门', dataIndex: 'deptName', key: 'deptName', width: 150 },
  { title: '岗位职务', dataIndex: 'postName', key: 'postName', width: 130 },
  { title: '绑定角色', dataIndex: 'roleNames', key: 'roleNames' },
  { title: '账号状态', dataIndex: 'status', key: 'status', width: 100, align: 'center' as const },
  { title: '操作', key: 'action', width: 190, align: 'center' as const }
];

const roleSelectOptions = computed(() => {
  return roleList.value.map(r => ({
    label: `${r.roleName} (${r.roleCode})`,
    value: r.id
  }));
});

const buildDeptTree = (items: DeptItem[], parentId: number = 0): any[] => {
  const result: any[] = [];
  for (const item of items) {
    if (Number(item.parentId || 0) === parentId) {
      const children = buildDeptTree(items, Number(item.id));
      result.push({
        title: item.deptName,
        key: item.id,
        value: item.id,
        children: children.length ? children : undefined
      });
    }
  }
  return result;
};

const loadDeptAndRoles = async () => {
  try {
    const dRes = await getDeptTreeApi();
    if (dRes.code === 200 && dRes.data) {
      deptTreeData.value = buildDeptTree(dRes.data, 0);
      deptSelectOptions.value = deptTreeData.value;
    }
  } catch {}

  try {
    const rRes = await listRolesApi();
    if (rRes.code === 200 && rRes.data) {
      roleList.value = rRes.data;
    }
  } catch {}
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await getUserPageApi({
      pageNum: pageNum.value,
      pageSize: 10,
      keyword: searchKeyword.value,
      deptId: selectedDeptId.value,
      status: filterStatus.value
    });
    if (res.code === 200 && res.data) {
      userList.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch {
    // 回退数据
    userList.value = [
      {
        id: 1,
        username: 'admin',
        realName: '超级管理员',
        workNo: 'OA0001',
        deptName: '云枢协同集团总部',
        postName: '总系统架构师',
        phone: '13800000001',
        email: 'admin@graddu.com',
        status: 1,
        roleNames: ['超级管理员']
      },
      {
        id: 2,
        username: 'zhangsan',
        realName: '张三',
        workNo: 'OA1001',
        deptName: '技术研发中心',
        postName: '后端开发工程师',
        phone: '13800000002',
        email: 'zhangsan@graddu.com',
        status: 1,
        roleNames: ['普通员工']
      }
    ];
    total.value = 2;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pageNum.value = 1;
  loadData();
};

const selectDept = (deptId?: number) => {
  selectedDeptId.value = deptId;
  selectedDeptKeys.value = deptId ? [deptId] : [];
  handleSearch();
};

const onDeptSelect = (selectedKeys: any[]) => {
  if (selectedKeys.length) {
    selectDept(selectedKeys[0]);
  } else {
    selectDept(undefined);
  }
};

const openCreateModal = () => {
  modalMode.value = 'create';
  formData.id = undefined;
  formData.username = '';
  formData.realName = '';
  formData.password = '';
  formData.workNo = 'OA' + Math.floor(Math.random() * 8999 + 1000);
  formData.deptId = selectedDeptId.value;
  formData.postName = '';
  formData.phone = '';
  formData.email = '';
  formData.roleIds = [];
  modalVisible.value = true;
};

const openEditModal = (record: UserVO) => {
  modalMode.value = 'edit';
  formData.id = record.id;
  formData.username = record.username;
  formData.realName = record.realName;
  formData.workNo = record.workNo || '';
  formData.deptId = record.deptId;
  formData.postName = record.postName || '';
  formData.phone = record.phone || '';
  formData.email = record.email || '';
  formData.roleIds = (record.roleIds as any) || [];
  modalVisible.value = true;
};

const handleModalSubmit = async () => {
  if (!formData.realName.trim()) {
    message.warning('请输入员工真实姓名');
    return;
  }
  try {
    submitting.value = true;
    if (modalMode.value === 'create') {
      if (!formData.username.trim()) {
        message.warning('请输入登录账号');
        return;
      }
      const res = await createUserApi(formData);
      if (res.code === 200) {
        message.success('新增员工成功，初始密码为 123456');
        modalVisible.value = false;
        loadData();
      }
    } else {
      const res = await updateUserApi(formData as any);
      if (res.code === 200) {
        message.success('员工资料修改成功');
        modalVisible.value = false;
        loadData();
      }
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  } finally {
    submitting.value = false;
  }
};

const handleStatusChange = async (userId: number, status: number) => {
  try {
    const res = await changeUserStatusApi(userId, status);
    if (res.code === 200) {
      message.success(status === 1 ? '已启用账号' : '已停用该账号');
      loadData();
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  }
};

const handleResetPassword = async (userId: number) => {
  try {
    const res = await resetUserPasswordApi(userId);
    if (res.code === 200) {
      message.success('密码已成功重置为 123456');
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  }
};

const handleDelete = async (userId: number) => {
  try {
    const res = await deleteUserApi(userId);
    if (res.code === 200) {
      message.success('员工删除成功');
      loadData();
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  }
};

onMounted(() => {
  loadDeptAndRoles();
  loadData();
});
</script>

<style scoped>
.system-page {
  padding: 0;
}

.dept-tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}
</style>
