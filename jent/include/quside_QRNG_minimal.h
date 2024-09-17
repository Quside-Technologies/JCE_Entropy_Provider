/*
 ============================================================================
 Name        : quside_QRNG_minimal.h
 Created on  : 27 May. 2024
 Version     : 0.1
 Copyright   : Copyright (C) 2024 QUSIDE TECHNOLOGIES - All Rights Reserved.
               Unauthorized copying of this file, via any medium is
               strictly prohibited.
 Description : This header defines the bare minimum C functionalities needed
               for using Quside's QRNG in cryptographic applications.
 ============================================================================
 */

#ifndef QUSIDE_QRNG_MINIMAL_H
#define QUSIDE_QRNG_MINIMAL_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>

/* This enumerate defines the codes of the possible status of the QRNG calibration. */
typedef enum {
	 DEFAULT,
	 CALIBRATING,
	 CALIB_SUCCED,
	 CALIB_FAIL,
	 I2C_ERROR
} quside_qrng_calibrationStatus;


/******************************************************************************
** find_boards
**
** Search Babylon devices connected through PCI in the system and initialize
** the lists with the descriptors.
**
** @return [uint16_t] number of Babylons connected.
******************************************************************************/
uint16_t find_boards(void);

/******************************************************************************
** get_random
**
** This function captures N uint32 bytes of extracted random numbers.
**
** @param mem_slot [uint32_t *] pointer to region where save the numbers.
** @param Nuint32 [const size_t] count of random numbers in bytes.
** @param devInd [uint16_t] Index of the device to use from the list.
**
** @return [int] If it success returns 0, otherwise -1.
******************************************************************************/
int get_random(uint32_t* mem_slot, const size_t Nuint32, const uint16_t devInd);

/******************************************************************************
** get_boards
**
** Returns a list of the devices IDs connected to the system.
**
** @param devIDs  [uint16_t**] List that will contain the devices IDs.
** @param numDevs [uint16_t*] Number of elements that the list will contain.
**
** @return void.
**
******************************************************************************/
void get_boards(uint16_t** devIDs, uint16_t* numDevs);

/******************************************************************************
** find_device
**
** Returns the index of a device contained in the devices list.
**
** @param devID [uint16_t] Device id to search in the list.
**
** @return [int] The index of the devID in the devices list. If the provided devID
**               does not exist, this function will return a -1.
******************************************************************************/
int find_device(const uint16_t devID);

/******************************************************************************
** quality_Qfactor
**
** Calculates Q Factor. This value is an statistical calcutaion of the quantic
** quality based on the running average of the output and the correlators.
**
** @param devInd [const uint16_t] Index of the device to control defined in
**                                devices array.
**
** @param qFactor [float*] Variable that will contains the qFactor value.
**
** @return [int] If it success 0, otherwise returns -1.
**
******************************************************************************/
int quality_Qfactor(const uint16_t devInd, float* qFactor);

/******************************************************************************
** get_hmin
**
** Gets the minimum entropy of the system. This value only changes after
** calibration.
**
** @param devInd [const uint16_t] Index of the device to control defined in
**                                devices array.
**
** @param hMin [float*] Variable that will contains the hMin value.
**
** @return [int] If it success 0, otherwise returns -1.
**
******************************************************************************/
int get_hmin(const uint16_t devInd, float* hMin);

/******************************************************************************
** get_calibration_status
**
** Gets the QRNG calibration status.
**
** @param devInd [const uint16_t] Index of the device to control defined in
**                                devices array.
**
** @param status [calibrationStatus*] Contains the status of the calibration
**                                    process.
**
** @return [int] If it success 0, otherwise returns -1.
**
******************************************************************************/
int get_calibration_status(const uint16_t devInd, quside_qrng_calibrationStatus* status);

/******************************************************************************
** set_calibration
**
** Sends an order to the QRNG to calibrate the system. This operation will block
** the QRNG until it finished the calibration.
**
** @param devInd [const uint16_t] Index of the device to control defined in
**                                devices array.
**
** @return [int] If it success 0, otherwise returns -1.
**
******************************************************************************/
int set_calibration(const uint16_t devInd);


#ifdef __cplusplus
}
#endif

#endif /* QUSIDE_QRNG_ADMIN_H */
