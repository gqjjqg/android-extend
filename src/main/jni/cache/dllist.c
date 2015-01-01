#include <stdio.h>
#include "dllist.h"

void dl_init_node(LPDL_NODE node)
{
	node->dl_next = NULL;
	node->dl_pre = NULL;
}

void dl_insert_node(LPDL_NODE node, LPDL_NODE insert, LPDL_ROOT root)
{
	if (insert) { //update head
		node->dl_next = insert->dl_next;
		insert->dl_next = node;
		node->dl_pre = insert;
		if (node->dl_next) {
			node->dl_next->dl_pre = node;
		} else { //update last
			root->dl_last = node;
		}
	} else {
		if (root->dl_head) {
			root->dl_head->dl_pre = node;
			node->dl_next = root->dl_head;
			root->dl_head = node;
			root->dl_head->dl_pre = NULL;
		} else {
			root->dl_head = node;
			node->dl_pre = NULL;
			root->dl_last = node;
			node->dl_next = NULL;
		}
	}
}

void dl_remove_node(LPDL_NODE node, LPDL_ROOT root)
{
	if (node->dl_pre) {
		node->dl_pre->dl_next = node->dl_next;
	} else {
		root->dl_head = node->dl_next;
	}

	if (node->dl_next) {
		node->dl_next->dl_pre = node->dl_pre;
	} else {
		root->dl_last = node->dl_pre;
	}
}

void dl_replace_node(LPDL_NODE node, LPDL_NODE replace, LPDL_ROOT root)
{
	if (node->dl_pre) {
		node->dl_pre->dl_next = replace;
		replace->dl_pre = node->dl_pre;
	} else {
		root->dl_head = replace;
	}

	if (node->dl_next) {
		node->dl_next->dl_pre = replace;
		replace->dl_next = node->dl_next;
	} else {
		root->dl_last = replace;
	}
}

LPDL_NODE dl_next(LPDL_NODE node)
{
	return node->dl_next;
}

LPDL_NODE dl_prev(LPDL_NODE node)
{
	return node->dl_pre;
}

LPDL_NODE dl_first(const LPDL_ROOT root)
{
	return root->dl_head;
}

LPDL_NODE dl_last(const LPDL_ROOT root)
{
	return root->dl_last;
}
