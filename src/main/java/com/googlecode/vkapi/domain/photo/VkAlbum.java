package com.googlecode.vkapi.domain.photo;

public class VkAlbum {
	private long albumId;
	private String name;
	private VkPhoto cover;
	private int size;

	public long getAlbumId() {
		return albumId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public VkPhoto getCover() {
		return cover;
	}

	public void setCover(VkPhoto cover) {
		this.cover = cover;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}