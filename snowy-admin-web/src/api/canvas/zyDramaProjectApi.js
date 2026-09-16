import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/canvas/dramaproject/` + url, ...arg)

/**
 * 短剧项目Api接口管理器
 *
 * @author hanbin
 * @date  2026/09/08 14:26
 **/
export default {
	// 获取短剧项目分页
	zyDramaProjectPage(data) {
		return request('page', data, 'get')
	},
	// 提交短剧项目表单 edit为true时为编辑，默认为新增
	zyDramaProjectSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除短剧项目
	zyDramaProjectDelete(data) {
		return request('delete', data)
	},
	// 获取短剧项目详情
	zyDramaProjectDetail(data) {
		return request('detail', data, 'get')
	},
	// 下载短剧项目导入模板
    zyDramaProjectDownloadTemplate(data) {
		return request('downloadImportTemplate', data, 'get', {
			responseType: 'blob'
		})
    },
	// 导入短剧项目
	zyDramaProjectImport(data) {
		return request('importData', data)
	},
	// 导出短剧项目
	zyDramaProjectExport(data) {
		return request('exportData', data, 'post', {
			responseType: 'blob'
		})
	},
}
