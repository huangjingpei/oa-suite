<template>
  <div class="address-book-page">
    <a-card :bordered="false" class="header-card">
      <div class="page-header">
        <div class="header-title">
          <ContactsOutlined class="title-icon" />
          <span class="title-text">企业全员通讯录</span>
          <a-tag color="blue" class="count-tag">在职员工 {{ userList.length }} 人</a-tag>
        </div>
        <div class="header-tools">
          <a-input-search
            v-model:value="searchKeyword"
            placeholder="搜索姓名 / 工号 / 手机 / 邮箱 / 岗位..."
            allow-clear
            style="width: 280px"
            @search="handleSearch"
            @change="handleKeywordChange"
          />
          <a-radio-group v-model:value="viewMode" button-style="solid">
            <a-radio-button value="card">
              <AppstoreOutlined /> 名片卡片
            </a-radio-button>
            <a-radio-button value="table">
              <BarsOutlined /> 清单列表
            </a-radio-button>
          </a-radio-group>
          <a-button @click="loadData">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </div>
      </div>
    </a-card>

    <a-row :gutter="16" style="margin-top: 16px">
      <!-- 左侧：部门架构检索树 -->
      <a-col :xs="24" :sm="24" :md="7" :lg="6">
        <a-card :bordered="false" title="组织架构导航" class="dept-card">
          <div
            class="all-dept-item"
            :class="{ active: currentDeptId === undefined }"
            @click="handleSelectAllDept"
          >
            <ApartmentOutlined style="margin-right: 8px" />
            <span>全公司所有部门</span>
          </div>
          <a-divider style="margin: 8px 0" />
          <a-tree
            v-if="deptTreeData.length > 0"
            :tree-data="deptTreeData"
            :field-names="{ title: 'deptName', key: 'id', children: 'children' }"
            default-expand-all
            :selected-keys="selectedDeptKeys"
            @select="handleSelectDept"
          />
          <a-empty v-else description="暂无部门架构" />
        </a-card>
      </a-col>

      <!-- 右侧：员工联系方式展示 -->
      <a-col :xs="24" :sm="24" :md="17" :lg="18">
        <a-card :bordered="false" class="content-card">
          <a-spin :spinning="loading">
            <!-- 模式 1：名片网格模式 -->
            <div v-if="viewMode === 'card'">
              <a-row v-if="userList.length > 0" :gutter="[16, 16]">
                <a-col
                  v-for="user in userList"
                  :key="user.id"
                  :xs="24"
                  :sm="12"
                  :md="12"
                  :lg="8"
                >
                  <div class="contact-card">
                    <div class="card-top">
                      <a-avatar
                        :size="52"
                        :style="{ backgroundColor: getAvatarColor(user.realName || user.username) }"
                      >
                        {{ (user.realName || user.username).charAt(0) }}
                      </a-avatar>
                      <div class="card-info">
                        <div class="user-name-row">
                          <span class="user-real-name">{{ user.realName }}</span>
                          <span class="user-gender">
                            <ManOutlined v-if="user.gender === 1" style="color: #1677ff" />
                            <WomanOutlined v-else-if="user.gender === 2" style="color: #eb2f96" />
                          </span>
                        </div>
                        <div class="user-post">{{ user.postName || '业务专员' }}</div>
                        <div class="user-dept">
                          <a-tag color="cyan">{{ user.deptName || '未归属' }}</a-tag>
                        </div>
                      </div>
                    </div>

                    <a-divider style="margin: 12px 0 10px 0" />

                    <div class="card-meta">
                      <div class="meta-item">
                        <IdcardOutlined class="meta-icon" />
                        <span class="meta-label">工号：</span>
                        <span class="meta-value">{{ user.workNo || 'OA0000' }}</span>
                      </div>
                      <div class="meta-item">
                        <PhoneOutlined class="meta-icon" />
                        <span class="meta-label">电话：</span>
                        <span class="meta-value">
                          <a v-if="user.phone" :href="`tel:${user.phone}`">{{ user.phone }}</a>
                          <span v-else style="color: #bfbfbf">暂无</span>
                        </span>
                        <a-button
                          v-if="user.phone"
                          type="link"
                          size="small"
                          class="copy-btn"
                          @click="copyText(user.phone, '手机号码')"
                        >
                          复制
                        </a-button>
                      </div>
                      <div class="meta-item">
                        <MailOutlined class="meta-icon" />
                        <span class="meta-label">邮箱：</span>
                        <span class="meta-value meta-email">
                          <a v-if="user.email" :href="`mailto:${user.email}`">{{ user.email }}</a>
                          <span v-else style="color: #bfbfbf">暂无</span>
                        </span>
                        <a-button
                          v-if="user.email"
                          type="link"
                          size="small"
                          class="copy-btn"
                          @click="copyText(user.email, '电子邮箱')"
                        >
                          复制
                        </a-button>
                      </div>
                    </div>

                    <div class="card-footer">
                      <a-button
                        type="dashed"
                        block
                        size="small"
                        @click="copyVCard(user)"
                      >
                        <CopyOutlined /> 复制完整联系人名片
                      </a-button>
                    </div>
                  </div>
                </a-col>
              </a-row>
              <a-empty v-else description="未找到符合条件的员工联系方式" orientation="vertical" />
            </div>

            <!-- 模式 2：表格清单模式 -->
            <div v-else>
              <a-table
                :columns="columns"
                :data-source="userList"
                row-key="id"
                :pagination="{ pageSize: 10, showTotal: (total: number) => `共 ${total} 人` }"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'realName'">
                    <a-space>
                      <a-avatar
                        size="small"
                        :style="{ backgroundColor: getAvatarColor(record.realName || record.username) }"
                      >
                        {{ (record.realName || record.username).charAt(0) }}
                      </a-avatar>
                      <span style="font-weight: 600">{{ record.realName }}</span>
                    </a-space>
                  </template>

                  <template v-else-if="column.key === 'deptName'">
                    <a-tag color="cyan">{{ record.deptName || '未归属' }}</a-tag>
                  </template>

                  <template v-else-if="column.key === 'phone'">
                    <span v-if="record.phone">
                      <a :href="`tel:${record.phone}`">{{ record.phone }}</a>
                      <a-button
                        type="link"
                        size="small"
                        @click="copyText(record.phone, '手机号')"
                      >
                        复制
                      </a-button>
                    </span>
                    <span v-else style="color: #bfbfbf">-</span>
                  </template>

                  <template v-else-if="column.key === 'email'">
                    <span v-if="record.email">
                      <a :href="`mailto:${record.email}`">{{ record.email }}</a>
                      <a-button
                        type="link"
                        size="small"
                        @click="copyText(record.email, '邮箱')"
                      >
                        复制
                      </a-button>
                    </span>
                    <span v-else style="color: #bfbfbf">-</span>
                  </template>

                  <template v-else-if="column.key === 'action'">
                    <a-button type="link" size="small" @click="copyVCard(record)">
                      <CopyOutlined /> 复制名片
                    </a-button>
                  </template>
                </template>
              </a-table>
            </div>
          </a-spin>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  ContactsOutlined,
  ReloadOutlined,
  ApartmentOutlined,
  AppstoreOutlined,
  BarsOutlined,
  PhoneOutlined,
  MailOutlined,
  IdcardOutlined,
  ManOutlined,
  WomanOutlined,
  CopyOutlined
} from '@ant-design/icons-vue';
import { getDeptTreeApi, getAddressBookApi, type DeptItem } from '@/api/system';
import type { UserVO } from '@/types/user';

const loading = ref(false);
const viewMode = ref<'card' | 'table'>('card');
const searchKeyword = ref('');
const currentDeptId = ref<number | undefined>(undefined);
const selectedDeptKeys = ref<number[]>([]);

const deptTreeData = ref<DeptItem[]>([]);
const userList = ref<UserVO[]>([]);

const columns = [
  { title: '员工姓名', dataIndex: 'realName', key: 'realName', width: 140 },
  { title: '工号', dataIndex: 'workNo', key: 'workNo', width: 110 },
  { title: '所属部门', dataIndex: 'deptName', key: 'deptName', width: 150 },
  { title: '职务岗位', dataIndex: 'postName', key: 'postName', width: 130 },
  { title: '手机号码', dataIndex: 'phone', key: 'phone', width: 160 },
  { title: '电子邮箱', dataIndex: 'email', key: 'email', width: 200 },
  { title: '操作', key: 'action', width: 120, align: 'center' as const }
];

// 根据名字分配色彩
const colors = ['#1677ff', '#722ed1', '#13c2c2', '#52c41a', '#fa8c16', '#eb2f96'];
const getAvatarColor = (name: string): string => {
  if (!name) return '#1677ff';
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return colors[Math.abs(hash) % colors.length];
};

// 加载部门架构
const loadDepts = async () => {
  try {
    const res = await getDeptTreeApi();
    if (res.code === 200 && res.data) {
      deptTreeData.value = res.data;
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  }
};

// 加载员工通讯录
const loadData = async () => {
  loading.value = true;
  try {
    const res = await getAddressBookApi({
      deptId: currentDeptId.value,
      keyword: searchKeyword.value.trim() || undefined
    });
    if (res.code === 200 && res.data) {
      userList.value = res.data;
    }
  } catch (err: any) {
    if (err?.message) message.error(err.message);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  loadData();
};

const handleKeywordChange = () => {
  if (!searchKeyword.value) {
    loadData();
  }
};

const handleSelectAllDept = () => {
  currentDeptId.value = undefined;
  selectedDeptKeys.value = [];
  loadData();
};

const handleSelectDept = (selectedKeys: any[]) => {
  if (selectedKeys.length > 0) {
    currentDeptId.value = selectedKeys[0];
    selectedDeptKeys.value = [selectedKeys[0]];
  } else {
    currentDeptId.value = undefined;
    selectedDeptKeys.value = [];
  }
  loadData();
};

// 复制单个属性
const copyText = (text: string, label: string) => {
  if (!text) return;
  navigator.clipboard.writeText(text).then(() => {
    message.success(`${label}已复制到剪贴板`);
  }).catch(() => {
    message.info(text);
  });
};

// 复制完整名片
const copyVCard = (u: UserVO) => {
  const cardText = `【员工名片】\n姓名：${u.realName || u.username}\n工号：${u.workNo || 'OA0000'}\n部门：${u.deptName || '未归属'}\n岗位：${u.postName || '标准职级'}\n手机：${u.phone || '暂无'}\n邮箱：${u.email || '暂无'}`;
  navigator.clipboard.writeText(cardText).then(() => {
    message.success(`已复制 [${u.realName}] 的联系人名片`);
  }).catch(() => {
    message.info(cardText);
  });
};

onMounted(() => {
  loadDepts();
  loadData();
});
</script>

<style scoped>
.address-book-page {
  padding: 0;
}

.header-card {
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 20px;
  color: #1677ff;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #1f1f1f;
}

.count-tag {
  font-weight: 500;
}

.header-tools {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dept-card {
  border-radius: 8px;
  min-height: 520px;
}

.all-dept-item {
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #333;
  transition: all 0.3s;
}

.all-dept-item:hover {
  background: #f0f7ff;
  color: #1677ff;
}

.all-dept-item.active {
  background: #e6f4ff;
  color: #1677ff;
  font-weight: 600;
}

.content-card {
  border-radius: 8px;
  min-height: 520px;
}

.contact-card {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.25s ease-in-out;
}

.contact-card:hover {
  border-color: #91caff;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.08);
  transform: translateY(-2px);
}

.card-top {
  display: flex;
  align-items: center;
  gap: 14px;
}

.card-info {
  flex: 1;
  min-width: 0;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-real-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.user-gender {
  font-size: 13px;
}

.user-post {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-dept {
  margin-top: 4px;
}

.card-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-icon {
  color: #94a3b8;
  margin-right: 6px;
  font-size: 14px;
}

.meta-label {
  color: #64748b;
  width: 44px;
}

.meta-value {
  color: #1e293b;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta-email a {
  color: #1677ff;
}

.copy-btn {
  padding: 0 4px;
  font-size: 12px;
  height: auto;
}

.card-footer {
  margin-top: 14px;
}
</style>
