import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/canvas/session/` + url, ...arg)

/**
 * 会话Api接口管理器
 *
 * @author hanbin
 * @date  2026/09/07 18:57
 **/
export default {
	// 获取会话分页
	zySessionPage(data) {
		return request('page', data, 'get')
	},
	// 提交会话表单 edit为true时为编辑，默认为新增
	zySessionSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除会话
	zySessionDelete(data) {
		return request('delete', data)
	},
	// 获取会话详情
	zySessionDetail(data) {
		return request('detail', data, 'get')
	},
	// 下载会话导入模板
    zySessionDownloadTemplate(data) {
		return request('downloadImportTemplate', data, 'get', {
			responseType: 'blob'
		})
    },
	// 导入会话
	zySessionImport(data) {
		return request('importData', data)
	},
	// 导出会话
	zySessionExport(data) {
		return request('exportData', data, 'post', {
			responseType: 'blob'
		})
	},
}
