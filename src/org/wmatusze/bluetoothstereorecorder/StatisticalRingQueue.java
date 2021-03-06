package org.wmatusze.bluetoothstereorecorder;

import java.util.LinkedList;
import java.util.Queue;

public class StatisticalRingQueue<E extends Number> {
	
	public int getSize() {
		return _queue.size();
	}
	
	public int getMaxSize() {
		return _size;
	}

	public double getAverage() {
		return _average;
	}

	public double getVariance() {
		return _variance;
	}

	public double getStandardDeviation() {
		return _standardDeviation;
	}

	public StatisticalRingQueue(int size) {
		_size = size;
	}
	
	public E push(E e) {
		_queue.add(e);
		
		double eValue = e.doubleValue();
		double queueSize = _queue.size();
		
		if(_queue.size() > _size) {
			E removed = _queue.remove();
			double removedValue = removed.doubleValue();
			
			_average -= (removedValue - _average) / _size;
			_average += (eValue - _average) / _size;
			
			_powerSumAverage -= (removedValue * removedValue - _powerSumAverage) / _size;
			_powerSumAverage += (eValue * eValue - _powerSumAverage) / queueSize;
			
			_variance = (_powerSumAverage * _size - _size * _average * _average) / (_size - 1);
			
			_standardDeviation = Math.sqrt(_variance);
			
			return removed;
		} else {
			_average += (eValue - _average) / queueSize;
			_powerSumAverage += (eValue * eValue - _powerSumAverage) / queueSize;
			_variance = (_powerSumAverage * queueSize - queueSize * _average * _average) / (queueSize - 1);
			_standardDeviation = Math.sqrt(_variance);
			
			return null;
		}
	}
	
	private Queue<E> _queue = new LinkedList<E>();
	private int _size;
	private double _average = 0;
	private double _variance = 0;
	private double _standardDeviation = 0;
	private double _powerSumAverage = 0;
}
