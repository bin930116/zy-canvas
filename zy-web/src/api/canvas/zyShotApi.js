import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/canvas/shot/` + url, ...arg)

/**
 * 短剧项目分镜Api接口管理器
 *
 * @author hanbin
 * @date  2026/09/07 18:55
 **/
export default {
	// 获取短剧项目分镜分页
	zyShotPage(data) {
		return request('page', data, 'get')
	},
	// 提交短剧项目分镜表单 edit为true时为编辑，默认为新增
	zyShotSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除短剧项目分镜
	zyShotDelete(data) {
		return request('delete', data)
	},
	// 获取短剧项目分镜详情
	zyShotDetail(data) {
		return request('detail', data, 'get')
	},
	// 下载短剧项目分镜导入模板
    zyShotDownloadTemplate(data) {
		return request('downloadImportTemplate', data, 'get', {
			responseType: 'blob'
		})
    },
	// 导入短剧项目分镜
	zyShotImport(data) {
		return request('importData', data)
	},
	// 导出短剧项目分镜
	zyShotExport(data) {
		return request('exportData', data, 'post', {
			responseType: 'blob'
		})
	},
}
