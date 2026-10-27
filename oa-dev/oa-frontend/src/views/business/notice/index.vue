<template>
  <div class="notice-management-container">
    <!-- 头部指标卡片 -->
    <a-row :gutter="16" class="stats-row">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic title="全部公文通告" :value="totalNotices" :value-style="{ color: '#1677ff' }">
            <template #prefix><FileTextOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic title="我未阅读通告" :value="unreadNoticesCount" :value-style="{ color: '#ff4d4f' }">
            <template #prefix><BellOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic title="已发布公示" :value="publishedCount" :value-style="{ color: '#52c41a' }">
            <template #prefix><CheckCircleOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic title="草稿箱" :value="draftCount" :value-style="{ color: '#faad14' }">
            <template #prefix><EditOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 检索与操作栏 -->
    <a-card :bordered="false" class="table-card">
      <div class="action-bar">
        <a-form layout="inline" :model="queryForm">
          <a-form-item label="关键字">
            <a-input
              v-model:value="queryForm.keyword"
              placeholder="搜索标题或发布人"
              allow-clear
              style="width: 200px"
              @pressEnter="handleSearch"
            />
          </a-form-item>
          <a-form-item label="公文分类">
            <a-select
              v-model:value="queryForm.category"
              placeholder="全部分类"
              allow-clear
              style="width: 140px"
              @change="handleSearch"
            >
              <a-select-option value="">全部</a-select-option>
              <a-select-option value="通知">通知</a-select-option>
              <a-select-option value="公告">公告</a-select-option>
              <a-select-option value="通报">通报</a-select-option>
              <a-select-option value="决议">决议</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="handleSearch">
                <template #icon><SearchOutlined /></template>
                查询
              </a-button>
              <a-button @click="handleReset">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>

        <a-button type="primary" @click="openPublishModal">
          <template #icon><PlusOutlined /></template>
          起草发布通告
        </a-button>
      </div>

      <!-- 通告表格 -->
      <a-table
        :columns="columns"
        :data-source="noticeList"
        :loading="loading"
        row-key="id"
        :pagination="{
          current: pageNum,
          pageSize: pageSize,
          total: totalNotices,
          showTotal: (total: number) => `共 ${total} 条公文通告`,
          onChange: handlePageChange
        }"
        style="margin-top: 16px"
      >
        <template #bodyCell="{ column, record }">
          <!-- 标题及未读标识 -->
          <template v-if="column.key === 'title'">
            <div style="display: flex; align-items: center; gap: 8px">
              <a-badge v-if="!record.isRead" status="error" title="当前未读" />
              <a-tag :color="getCategoryColor(record.category)">{{ record.category || '通告' }}</a-tag>
              <a
                href="javascript:;"
                class="notice-title"
                :class="{ 'notice-unread': !record.isRead }"
                @click="viewNotice(record)"
              >
                {{ record.title }}
              </a>
              <a-tag v-if="record.priority === 'H'" color="red" size="small">紧急</a-tag>
              <a-tag v-else-if="record.priority === 'M'" color="orange" size="small">一般</a-tag>
            </div>
          </template>

          <!-- 查阅统计与进度条 -->
          <template v-else-if="column.key === 'readRate'">
            <div style="width: 140px">
              <div style="display: flex; justify-content: space-between; font-size: 12px; margin-bottom: 2px">
                <span style="color: #8c8c8c">已阅率</span>
                <span style="font-weight: bold; color: #1677ff">{{ record.readRate || 0 }}%</span>
              </div>
              <a-progress
                :percent="record.readRate || 0"
                size="small"
                :status="record.readRate === 100 ? 'success' : 'active'"
                :show-info="false"
              />
              <div style="font-size: 11px; color: #bfbfbf; text-align: right">
                {{ record.readCount || 0 }} / {{ record.totalCount || 0 }} 人已查阅
              </div>
            </div>
          </template>

          <!-- 发布状态 -->
          <template v-else-if="column.key === 'status'">
            <a-tag v-if="record.status === 1" color="green">正常发布</a-tag>
            <a-tag v-else-if="record.status === 0" color="default">草稿待发</a-tag>
            <a-tag v-else-if="record.status === 2" color="red">已下架</a-tag>
          </template>

          <!-- 操作栏 -->
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button type="link" size="small" @click="viewNotice(record)">
                <template #icon><EyeOutlined /></template>
                查看
              </a-button>
              <a-button type="link" size="small" style="color: #722ed1" @click="openReadStats(record)">
                <template #icon><BarChartOutlined /></template>
                阅读追踪
              </a-button>
              <a-popconfirm
                title="确定删除该公文通告吗？"
                ok-text="确认删除"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" danger size="small">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 通告详情阅读弹窗 (自动标记为已读) -->
    <a-modal
      v-model:open="detailVisible"
      :title="null"
      :footer="null"
      width="780px"
      wrap-class-name="notice-detail-modal"
    >
      <div v-if="currentNotice" class="notice-detail-container">
        <div class="notice-header">
          <div class="notice-meta-tags">
            <a-tag :color="getCategoryColor(currentNotice.category)" style="font-size: 13px; padding: 2px 8px">
              {{ currentNotice.category || '通告' }}
            </a-tag>
            <a-tag v-if="currentNotice.priority === 'H'" color="red">紧急高优先级</a-tag>
            <a-tag v-else-if="currentNotice.priority === 'M'" color="orange">普通中优先级</a-tag>
            <a-tag v-else color="blue">低优先级</a-tag>
          </div>
          <h2 class="detail-title">{{ currentNotice.title }}</h2>
          <div class="detail-sub-meta">
            <span><strong>发布单位/人员：</strong>{{ currentNotice.publisherName }} ({{ currentNotice.publisherUsername }})</span>
            <span><strong>发布时间：</strong>{{ currentNotice.publishTime || currentNotice.createTime }}</span>
            <span style="color: #52c41a">
              <CheckCircleFilled /> 全员已阅 {{ currentNotice.readCount }} / {{ currentNotice.totalCount }} 人 ({{ currentNotice.readRate }}%)
            </span>
          </div>
        </div>
        <a-divider style="margin: 16px 0" />
        <div class="notice-body">
          <div class="notice-content-box">{{ currentNotice.content }}</div>
        </div>
        <div class="notice-footer-action">
          <a-space>
            <a-button type="default" @click="detailVisible = false">关闭窗口</a-button>
            <a-button type="primary" @click="openReadStatsFromDetail">
              <template #icon><BarChartOutlined /></template>
              查看全员阅读追踪
            </a-button>
          </a-space>
        </div>
      </div>
    </a-modal>

    <!-- 全员阅读状态追踪与一键催办弹窗 -->
    <a-modal
      v-model:open="statsVisible"
      title="公文通告全员查阅状态追踪矩阵"
      width="820px"
      :footer="null"
    >
      <div v-if="statsLoading" style="text-align: center; padding: 40px 0">
        <a-spin size="large" tip="正在生成全员查阅分析矩阵..." />
      </div>
      <div v-else-if="readStats" class="stats-matrix-container">
        <!-- 统计面板 -->
        <a-card :bordered="false" style="background: #fafafa; margin-bottom: 16px; border-radius: 8px">
          <a-row :gutter="24" align="middle">
            <a-col :span="6" style="text-align: center">
              <a-progress
                type="circle"
                :percent="readStats.readRate"
                :width="90"
                :status="readStats.readRate === 100 ? 'success' : 'normal'"
              />
            </a-col>
            <a-col :span="12">
              <h3 style="margin: 0 0 8px 0; font-size: 16px; font-weight: 600">{{ readStats.title }}</h3>
              <div style="color: #595959; font-size: 13px; line-height: 22px">
                应查阅总人数：<strong>{{ readStats.totalUsers }}</strong> 人<br />
                已阅读确认：<span style="color: #52c41a; font-weight: bold">{{ readStats.readCount }}</span> 人
                &nbsp;|&nbsp;
                未查阅待看：<span style="color: #ff4d4f; font-weight: bold">{{ readStats.unreadCount }}</span> 人
              </div>
            </a-col>
            <a-col :span="6" style="text-align: right">
              <a-popconfirm
                title="确定向所有尚未查阅该通告的人员发送加急催促提醒吗？"
                ok-text="确认催办"
                cancel-text="取消"
                :disabled="readStats.unreadCount === 0"
                @confirm="handleRemindUnread"
              >
                <a-button
                  type="primary"
                  danger
                  :disabled="readStats.unreadCount === 0"
                  :loading="reminding"
                >
                  <template #icon><NotificationOutlined /></template>
                  一键催办未读人员
                </a-button>
              </a-popconfirm>
            </a-col>
          </a-row>
        </a-card>

        <!-- 名单 Tab 切换 -->
        <a-tabs default-active-key="unread">
          <a-tab-pane key="unread">
            <template #tab>
              <span>
                <CloseCircleOutlined style="color: #ff4d4f" />
                未阅读人员名单
                <a-badge :count="readStats.unreadCount" :number-style="{ backgroundColor: '#ff4d4f' }" style="margin-left: 6px" />
              </span>
            </template>
            <a-table
              :columns="statsUserColumns"
              :data-source="readStats.unreadUsers"
              row-key="userId"
              size="middle"
              :pagination="{ pageSize: 5 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'status'">
                  <a-tag color="error">待查阅</a-tag>
                </template>
                <template v-else-if="column.key === 'time'">
                  <span style="color: #bfbfbf">- 尚未查阅 -</span>
                </template>
              </template>
            </a-table>
          </a-tab-pane>

          <a-tab-pane key="read">
            <template #tab>
              <span>
                <CheckCircleOutlined style="color: #52c41a" />
                已阅读人员名单
                <a-badge :count="readStats.readCount" :number-style="{ backgroundColor: '#52c41a' }" style="margin-left: 6px" />
              </span>
            </template>
            <a-table
              :columns="statsUserColumns"
              :data-source="readStats.readUsers"
              row-key="userId"
              size="middle"
              :pagination="{ pageSize: 5 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'status'">
                  <a-tag color="success">已查阅确认</a-tag>
                </template>
                <template v-else-if="column.key === 'time'">
                  <span style="color: #52c41a">{{ record.readTime }}</span>
                </template>
              </template>
            </a-table>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>

    <!-- 起草/发布通告弹窗 -->
    <a-modal
      v-model:open="publishVisible"
      title="起草并发布企业公文通告"
      width="650px"
      :confirm-loading="publishing"
      @ok="handleSaveNotice(1)"
    >
      <a-form :model="publishForm" layout="vertical" style="margin-top: 16px">
        <a-form-item label="公文通告标题" required>
          <a-input v-model:value="publishForm.title" placeholder="请输入通告公文名称（如：关于xxxx的通知）" />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="分类类型" required>
              <a-select v-model:value="publishForm.category">
                <a-select-option value="通知">通知</a-select-option>
                <a-select-option value="公告">公告</a-select-option>
                <a-select-option value="通报">通报</a-select-option>
                <a-select-option value="决议">决议</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="紧急度优先级" required>
              <a-radio-group v-model:value="publishForm.priority" button-style="solid">
                <a-radio-button value="L">普通 (低)</a-radio-button>
                <a-radio-button value="M">常规 (中)</a-radio-button>
                <a-radio-button value="H">加急 (高)</a-radio-button>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="公示受众范围">
          <a-radio-group v-model:value="publishForm.targetScope">
            <a-radio value="ALL">全员通告（全体企业员工）</a-radio>
            <a-radio value="DEPT">特定部门</a-radio>
            <a-radio value="USER">指定人员</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="通告详细正文内容" required>
          <a-textarea
            v-model:value="publishForm.content"
            placeholder="请输入公文通告的正文内容、规章条款、通知详情..."
            :rows="6"
          />
        </a-form-item>
      </a-form>

      <template #footer>
        <a-space>
          <a-button @click="publishVisible = false">取消</a-button>
          <a-button :loading="publishing" @click="handleSaveNotice(0)">存为草稿</a-button>
          <a-button type="primary" :loading="publishing" @click="handleSaveNotice(1)">确认公开发布</a-button>
        </a-space>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  FileTextOutlined,
  BellOutlined,
  CheckCircleOutlined,
  CheckCircleFilled,
  EditOutlined,
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  EyeOutlined,
  BarChartOutlined,
  NotificationOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue';
import {
  pageNoticesApi,
  getNoticeDetailApi,
  getNoticeReadStatsApi,
  saveNoticeApi,
  deleteNoticeApi,
  remindUnreadUsersApi
} from '@/api/business';
import type { NoticeVO, NoticeSaveDTO, NoticeReadStatsVO } from '@/types/business';

// 检索表单与分页
const queryForm = reactive({
  keyword: '',
  category: ''
});

const pageNum = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const noticeList = ref<NoticeVO[]>([]);
const totalNotices = ref(0);

// 统计指标
const unreadNoticesCount = computed(() => noticeList.value.filter(n => !n.isRead).length);
const publishedCount = computed(() => noticeList.value.filter(n => n.status === 1).length);
const draftCount = computed(() => noticeList.value.filter(n => n.status === 0).length);

// 表格列配置
const columns = [
  { title: '公文通告标题', key: 'title', width: 340 },
  { title: '拟发人员', dataIndex: 'publisherName', key: 'publisherName', width: 120 },
  { title: '发布时间', dataIndex: 'publishTime', key: 'publishTime', width: 170 },
  { title: '全员阅读进度', key: 'readRate', width: 160 },
  { title: '状态', key: 'status', width: 100, align: 'center' as const },
  { title: '操作', key: 'action', width: 220, align: 'center' as const }
];

// 追踪用户列配置
const statsUserColumns = [
  { title: '姓名', dataIndex: 'realName', key: 'realName', width: 120 },
  { title: '登录账号', dataIndex: 'username', key: 'username', width: 120 },
  { title: '所属部门', dataIndex: 'deptName', key: 'deptName', width: 150 },
  { title: '查阅状态', key: 'status', width: 120, align: 'center' as const },
  { title: '查阅时间', key: 'time', width: 180 }
];

const getCategoryColor = (cat?: string) => {
  switch (cat) {
    case '通报': return 'red';
    case '决议': return 'purple';
    case '公告': return 'orange';
    case '通知':
    default:
      return 'blue';
  }
};

// 数据加载
const loadNotices = async () => {
  loading.value = true;
  try {
    const res = await pageNoticesApi({
      keyword: queryForm.keyword,
      category: queryForm.category,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    });
    noticeList.value = res.data.records || [];
    totalNotices.value = res.data.total || 0;
  } catch (err: any) {
    message.error(err.message || '加载公文通告列表失败');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pageNum.value = 1;
  loadNotices();
};

const handleReset = () => {
  queryForm.keyword = '';
  queryForm.category = '';
  pageNum.value = 1;
  loadNotices();
};

const handlePageChange = (page: number, size: number) => {
  pageNum.value = page;
  pageSize.value = size;
  loadNotices();
};

// 查看通告详情（自动标记已读）
const detailVisible = ref(false);
const currentNotice = ref<NoticeVO | null>(null);

const viewNotice = async (record: NoticeVO) => {
  try {
    const res = await getNoticeDetailApi(record.id);
    currentNotice.value = res.data;
    detailVisible.value = true;
    // 更新本地列表项状态为已读
    record.isRead = true;
  } catch (err: any) {
    message.error(err.message || '获取通告详情失败');
  }
};

// 阅读追踪状态
const statsVisible = ref(false);
const statsLoading = ref(false);
const readStats = ref<NoticeReadStatsVO | null>(null);
const reminding = ref(false);

const openReadStats = async (record: NoticeVO) => {
  statsVisible.value = true;
  statsLoading.value = true;
  try {
    const res = await getNoticeReadStatsApi(record.id);
    readStats.value = res.data;
  } catch (err: any) {
    message.error(err.message || '获取阅读追踪数据失败');
  } finally {
    statsLoading.value = false;
  }
};

const openReadStatsFromDetail = () => {
  if (currentNotice.value) {
    detailVisible.value = false;
    openReadStats(currentNotice.value);
  }
};

// 一键催办未读人员
const handleRemindUnread = async () => {
  if (!readStats.value) return;
  reminding.value = true;
  try {
    const res = await remindUnreadUsersApi(readStats.value.noticeId);
    message.success(res.data || '已成功向所有未阅读人员发送催促提醒！');
  } catch (err: any) {
    message.error(err.message || '催办通知发送失败');
  } finally {
    reminding.value = false;
  }
};

// 删除通告
const handleDelete = async (id: number) => {
  try {
    await deleteNoticeApi(id);
    message.success('公文通告已成功删除');
    loadNotices();
  } catch (err: any) {
    message.error(err.message || '删除通告失败');
  }
};

// 起草发布通告
const publishVisible = ref(false);
const publishing = ref(false);
const publishForm = reactive<NoticeSaveDTO>({
  title: '',
  category: '通知',
  priority: 'M',
  targetScope: 'ALL',
  content: '',
  status: 1
});

const openPublishModal = () => {
  publishForm.title = '';
  publishForm.category = '通知';
  publishForm.priority = 'M';
  publishForm.targetScope = 'ALL';
  publishForm.content = '';
  publishForm.status = 1;
  publishVisible.value = true;
};

const handleSaveNotice = async (status: number) => {
  if (!publishForm.title?.trim()) {
    message.warning('请填写公文通告标题');
    return;
  }
  if (!publishForm.content?.trim()) {
    message.warning('请填写通告正文内容');
    return;
  }
  publishForm.status = status;
  publishing.value = true;
  try {
    await saveNoticeApi(publishForm);
    message.success(status === 1 ? '公文通告发布成功！已全员同步' : '已保存为草稿');
    publishVisible.value = false;
    loadNotices();
  } catch (err: any) {
    message.error(err.message || '保存通告失败');
  } finally {
    publishing.value = false;
  }
};

onMounted(() => {
  loadNotices();
});
</script>

<style scoped>
.notice-management-container {
  padding: 0;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.notice-title {
  color: #262626;
  font-weight: 500;
  transition: color 0.2s;
}

.notice-title:hover {
  color: #1677ff;
}

.notice-unread {
  font-weight: 600;
  color: #000;
}

.notice-detail-container {
  padding: 8px 12px;
}

.notice-header {
  text-align: center;
}

.notice-meta-tags {
  margin-bottom: 12px;
}

.detail-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f1f1f;
  margin: 0 0 12px 0;
}

.detail-sub-meta {
  display: flex;
  justify-content: center;
  gap: 20px;
  font-size: 13px;
  color: #8c8c8c;
  flex-wrap: wrap;
}

.notice-body {
  padding: 12px 0;
}

.notice-content-box {
  background: #fdfdfd;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 20px;
  font-size: 14px;
  line-height: 1.8;
  color: #262626;
  white-space: pre-wrap;
  min-height: 160px;
}

.notice-footer-action {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.stats-matrix-container {
  padding: 4px 0;
}
</style>
