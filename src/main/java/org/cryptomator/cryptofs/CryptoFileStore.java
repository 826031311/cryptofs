/*******************************************************************************
 * Copyright (c) 2016 Sebastian Stenzel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the accompanying LICENSE.txt.
 *
 * Contributors:
 *     Sebastian Stenzel - initial API and implementation
 *******************************************************************************/
package org.cryptomator.cryptofs;

import static org.cryptomator.cryptofs.UncheckedThrows.rethrowUnchecked;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

@PerFileSystem
class CryptoFileStore extends DelegatingFileStore {

	private static final String VIEW_NAME_BASIC = "basic";
	private static final String VIEW_NAME_OWNER = "owner";
	private static final String VIEW_NAME_POSIX = "posix";
	private static final String VIEW_NAME_DOS = "dos";
	private static final String[] VIEW_NAMES = {VIEW_NAME_BASIC, VIEW_NAME_OWNER, VIEW_NAME_POSIX, VIEW_NAME_DOS};

	private final Set<Class<? extends FileAttributeView>> supportedFileAttributeViewTypes;

	@Inject
	public CryptoFileStore(@PathToVault Path pathToVault, CryptoFileAttributeViewProvider attributeViewProvider) {
		super(rethrowUnchecked(IOException.class).from(() -> Files.getFileStore(pathToVault)));
		this.supportedFileAttributeViewTypes = attributeViewProvider.knownFileAttributeViewTypes().stream().filter(super::supportsFileAttributeView).collect(Collectors.toSet());
	}

	@Override
	public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
		return supportedFileAttributeViewTypes.stream().filter(type::isAssignableFrom).findAny().isPresent();
	}

	@Override
	public boolean supportsFileAttributeView(String name) {
		switch (name) {
		case VIEW_NAME_BASIC:
			return supportsFileAttributeView(BasicFileAttributeView.class);
		case VIEW_NAME_OWNER:
			return supportsFileAttributeView(FileOwnerAttributeView.class);
		case VIEW_NAME_POSIX:
			return supportsFileAttributeView(PosixFileAttributeView.class);
		case VIEW_NAME_DOS:
			return supportsFileAttributeView(DosFileAttributeView.class);
		default:
			return false;
		}
	}

	Set<Class<? extends FileAttributeView>> supportedFileAttributeViewTypes() {
		return supportedFileAttributeViewTypes;
	}

	Set<String> supportedFileAttributeViewNames() {
		return Arrays.stream(VIEW_NAMES).filter(this::supportsFileAttributeView).collect(Collectors.toSet());
	}

}
